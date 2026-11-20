export interface Env {
  DB: D1Database;
  API_FOOTBALL_KEY?: string;
}

const CORS_HEADERS: Record<string, string> = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Content-Type": "application/json; charset=utf-8"
};

function json(data: unknown, status = 200, extra: Record<string, string> = {}): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...CORS_HEADERS, ...extra }
  });
}

function initials(name: string): string {
  return name
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toLocaleUpperCase("pt-BR") ?? "")
    .join("");
}

function matchDto(row: Record<string, unknown>) {
  const home = String(row.home_team ?? "Mandante");
  const away = String(row.away_team ?? "Visitante");
  const isLive = String(row.status ?? "") === "LIVE";
  const score = (value: unknown) => value === null || value === undefined ? "—" : String(value);
  const kickoff = String(row.kickoff_at ?? "—");
  const parsedDate = new Date(kickoff);
  const startTime = Number.isNaN(parsedDate.getTime())
    ? kickoff
    : new Intl.DateTimeFormat("pt-BR", { hour: "2-digit", minute: "2-digit", timeZone: "America/Sao_Paulo" }).format(parsedDate);

  return {
    id: String(row.id),
    sport: String(row.sport_name ?? "Futebol"),
    league: String(row.competition_name ?? "Competição"),
    country: String(row.country ?? ""),
    homeTeam: home,
    awayTeam: away,
    homeInitials: initials(home),
    awayInitials: initials(away),
    homeScore: score(row.home_score),
    awayScore: score(row.away_score),
    status: isLive ? "AO VIVO" : String(row.status ?? "AGENDADO"),
    clock: row.clock ? `${row.clock}'` : (isLive ? "AO VIVO" : "—"),
    startTime,
    isLive
  };
}

const MATCH_SELECT = `
  SELECT
    m.id,
    m.status,
    m.clock,
    m.kickoff_at,
    m.home_score,
    m.away_score,
    s.name AS sport_name,
    c.name AS competition_name,
    c.country AS country,
    h.name AS home_team,
    a.name AS away_team
  FROM matches m
  JOIN sports s ON s.id = m.sport_id
  LEFT JOIN competitions c ON c.id = m.competition_id
  JOIN teams h ON h.id = m.home_team_id
  JOIN teams a ON a.id = m.away_team_id
`;

async function liveMatches(env: Env): Promise<Response> {
  const result = await env.DB.prepare(
    `${MATCH_SELECT} WHERE m.status = 'LIVE' ORDER BY m.kickoff_at ASC LIMIT 100`
  ).all<Record<string, unknown>>();

  return json({ source: "cloudflare-d1", matches: (result.results ?? []).map(matchDto) }, 200, {
    "Cache-Control": "public, max-age=15"
  });
}

async function listMatches(url: URL, env: Env): Promise<Response> {
  const date = url.searchParams.get("date");
  const sport = url.searchParams.get("sport");
  const status = url.searchParams.get("status");
  const filters: string[] = [];
  const values: string[] = [];

  if (date) {
    filters.push("date(m.kickoff_at) = date(?)");
    values.push(date);
  }
  if (sport) {
    filters.push("s.id = ?");
    values.push(sport);
  }
  if (status) {
    filters.push("m.status = ?");
    values.push(status.toUpperCase());
  }

  const where = filters.length ? `WHERE ${filters.join(" AND ")}` : "";
  const query = env.DB.prepare(`${MATCH_SELECT} ${where} ORDER BY m.kickoff_at ASC LIMIT 200`)
    .bind(...values);
  const result = await query.all<Record<string, unknown>>();
  return json({ matches: (result.results ?? []).map(matchDto) });
}

async function matchDetails(id: string, env: Env): Promise<Response> {
  const row = await env.DB.prepare(`${MATCH_SELECT} WHERE m.id = ? LIMIT 1`)
    .bind(id)
    .first<Record<string, unknown>>();
  if (!row) return json({ error: "Partida não encontrada" }, 404);

  const [events, stats, players] = await Promise.all([
    env.DB.prepare(`
      SELECT e.minute, e.added_time, e.event_type, e.description,
             t.name AS team, p.name AS player, rp.name AS related_player
      FROM match_events e
      LEFT JOIN teams t ON t.id = e.team_id
      LEFT JOIN players p ON p.id = e.player_id
      LEFT JOIN players rp ON rp.id = e.related_player_id
      WHERE e.match_id = ? ORDER BY e.minute ASC, e.added_time ASC
    `).bind(id).all<Record<string, unknown>>(),
    env.DB.prepare(`
      SELECT statistic_key AS name, home_value AS home, away_value AS away, unit
      FROM match_statistics WHERE match_id = ? ORDER BY statistic_key
    `).bind(id).all<Record<string, unknown>>(),
    env.DB.prepare(`
      SELECT p.id, p.name, p.position, p.shirt_number, t.name AS team,
             ps.rating, ps.minutes_played, ps.goals, ps.assists, ps.shots,
             ps.key_passes, ps.tackles, ps.saves
      FROM player_match_stats ps
      JOIN players p ON p.id = ps.player_id
      LEFT JOIN teams t ON t.id = p.team_id
      WHERE ps.match_id = ? ORDER BY ps.rating DESC
    `).bind(id).all<Record<string, unknown>>()
  ]);

  return json({
    match: matchDto(row),
    events: events.results ?? [],
    statistics: stats.results ?? [],
    players: players.results ?? []
  });
}

async function standings(competitionId: string, env: Env): Promise<Response> {
  const result = await env.DB.prepare(`
    SELECT st.position, t.name AS team, st.played, st.won, st.drawn, st.lost,
           st.goals_for, st.goals_against, (st.goals_for - st.goals_against) AS goal_difference,
           st.points
    FROM standings st
    JOIN teams t ON t.id = st.team_id
    WHERE st.competition_id = ?
    ORDER BY st.position ASC
  `).bind(competitionId).all<Record<string, unknown>>();
  return json({ competitionId, table: result.results ?? [] });
}

async function comparePlayers(url: URL, env: Env): Promise<Response> {
  const ids = (url.searchParams.get("ids") ?? "")
    .split(",")
    .map((id) => id.trim())
    .filter(Boolean)
    .slice(0, 2);
  if (ids.length !== 2) {
    return json({ error: "Informe exatamente dois IDs em ?ids=atleta1,atleta2" }, 400);
  }

  const result = await env.DB.prepare(`
    SELECT p.id, p.name, p.position, t.name AS team,
           COUNT(ps.id) AS matches,
           ROUND(AVG(ps.rating), 2) AS average_rating,
           SUM(ps.goals) AS goals,
           SUM(ps.assists) AS assists,
           SUM(ps.shots) AS shots,
           SUM(ps.key_passes) AS key_passes,
           SUM(ps.tackles) AS tackles,
           SUM(ps.saves) AS saves
    FROM players p
    LEFT JOIN teams t ON t.id = p.team_id
    LEFT JOIN player_match_stats ps ON ps.player_id = p.id
    WHERE p.id IN (?, ?)
    GROUP BY p.id, p.name, p.position, t.name
  `).bind(ids[0], ids[1]).all<Record<string, unknown>>();

  return json({ players: result.results ?? [] });
}

async function apiFootballLive(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
  if (!env.API_FOOTBALL_KEY) {
    return json({
      error: "Integração ainda não configurada",
      help: "Adicione a chave como secret API_FOOTBALL_KEY no Cloudflare. O modo demonstrativo continua disponível."
    }, 503);
  }

  const cacheKey = new Request(new URL(request.url).toString(), { method: "GET" });
  const cached = await caches.default.match(cacheKey);
  if (cached) return cached;

  const upstream = await fetch("https://v3.football.api-sports.io/fixtures?live=all", {
    headers: { "x-apisports-key": env.API_FOOTBALL_KEY }
  });
  if (!upstream.ok) {
    return json({ error: "A fonte esportiva retornou um erro", providerStatus: upstream.status }, 502);
  }

  const payload = await upstream.json() as {
    response?: Array<{
      fixture?: { id?: number; status?: { short?: string; elapsed?: number | null } };
      league?: { name?: string; country?: string };
      teams?: { home?: { name?: string }; away?: { name?: string } };
      goals?: { home?: number | null; away?: number | null };
    }>;
  };

  const matches = (payload.response ?? []).map((item, index) => {
    const home = item.teams?.home?.name ?? "Mandante";
    const away = item.teams?.away?.name ?? "Visitante";
    const elapsed = item.fixture?.status?.elapsed;
    return {
      id: String(item.fixture?.id ?? `provider-${index}`),
      sport: "Futebol",
      league: item.league?.name ?? "Competição",
      country: item.league?.country ?? "",
      homeTeam: home,
      awayTeam: away,
      homeInitials: initials(home),
      awayInitials: initials(away),
      homeScore: String(item.goals?.home ?? 0),
      awayScore: String(item.goals?.away ?? 0),
      status: "AO VIVO",
      clock: elapsed ? `${elapsed}'` : (item.fixture?.status?.short ?? "AO VIVO"),
      startTime: "—",
      isLive: true
    };
  });

  const response = json({ source: "api-football", matches }, 200, { "Cache-Control": "public, max-age=60" });
  ctx.waitUntil(caches.default.put(cacheKey, response.clone()));
  return response;
}

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);
    const path = url.pathname.replace(/\/+$/, "") || "/";

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }
    if (request.method !== "GET") return json({ error: "Método não permitido" }, 405);

    try {
      if (path === "/api/health") {
        return json({ ok: true, app: "ArenaLive API", timestamp: new Date().toISOString() });
      }
      if (path === "/api/matches/live") return await liveMatches(env);
      if (path === "/api/matches") return await listMatches(url, env);
      if (path.startsWith("/api/matches/")) {
        const id = decodeURIComponent(path.slice("/api/matches/".length));
        return await matchDetails(id, env);
      }
      if (path.startsWith("/api/standings/")) {
        const id = decodeURIComponent(path.slice("/api/standings/".length));
        return await standings(id, env);
      }
      if (path === "/api/players/compare") return await comparePlayers(url, env);
      if (path === "/api/providers/api-football/live") return await apiFootballLive(request, env, ctx);
      return json({ error: "Rota não encontrada" }, 404);
    } catch (error) {
      console.error("ArenaLive API error", error);
      return json({ error: "Erro interno ao consultar os dados" }, 500);
    }
  }
};
