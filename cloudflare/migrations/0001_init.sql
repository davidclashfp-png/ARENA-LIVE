PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS sports (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  icon TEXT NOT NULL DEFAULT 'sport'
);

CREATE TABLE IF NOT EXISTS competitions (
  id TEXT PRIMARY KEY,
  sport_id TEXT NOT NULL REFERENCES sports(id),
  name TEXT NOT NULL,
  country TEXT NOT NULL DEFAULT '',
  season TEXT NOT NULL DEFAULT '',
  logo_url TEXT
);

CREATE TABLE IF NOT EXISTS teams (
  id TEXT PRIMARY KEY,
  sport_id TEXT NOT NULL REFERENCES sports(id),
  name TEXT NOT NULL,
  short_name TEXT,
  country TEXT,
  logo_url TEXT
);

CREATE TABLE IF NOT EXISTS players (
  id TEXT PRIMARY KEY,
  team_id TEXT REFERENCES teams(id),
  name TEXT NOT NULL,
  position TEXT,
  shirt_number INTEGER,
  photo_url TEXT
);

CREATE TABLE IF NOT EXISTS matches (
  id TEXT PRIMARY KEY,
  sport_id TEXT NOT NULL REFERENCES sports(id),
  competition_id TEXT REFERENCES competitions(id),
  home_team_id TEXT NOT NULL REFERENCES teams(id),
  away_team_id TEXT NOT NULL REFERENCES teams(id),
  kickoff_at TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'SCHEDULED',
  clock TEXT,
  home_score INTEGER,
  away_score INTEGER,
  round_name TEXT,
  venue TEXT,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS match_events (
  id TEXT PRIMARY KEY,
  match_id TEXT NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
  minute INTEGER,
  added_time INTEGER,
  event_type TEXT NOT NULL,
  team_id TEXT REFERENCES teams(id),
  player_id TEXT REFERENCES players(id),
  related_player_id TEXT REFERENCES players(id),
  description TEXT
);

CREATE TABLE IF NOT EXISTS match_statistics (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  match_id TEXT NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
  statistic_key TEXT NOT NULL,
  home_value REAL,
  away_value REAL,
  unit TEXT DEFAULT '',
  UNIQUE(match_id, statistic_key)
);

CREATE TABLE IF NOT EXISTS player_match_stats (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  match_id TEXT NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
  player_id TEXT NOT NULL REFERENCES players(id),
  rating REAL,
  minutes_played INTEGER,
  goals INTEGER DEFAULT 0,
  assists INTEGER DEFAULT 0,
  shots INTEGER DEFAULT 0,
  key_passes INTEGER DEFAULT 0,
  tackles INTEGER DEFAULT 0,
  saves INTEGER DEFAULT 0,
  UNIQUE(match_id, player_id)
);

CREATE TABLE IF NOT EXISTS standings (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  competition_id TEXT NOT NULL REFERENCES competitions(id),
  team_id TEXT NOT NULL REFERENCES teams(id),
  position INTEGER NOT NULL,
  played INTEGER DEFAULT 0,
  won INTEGER DEFAULT 0,
  drawn INTEGER DEFAULT 0,
  lost INTEGER DEFAULT 0,
  goals_for INTEGER DEFAULT 0,
  goals_against INTEGER DEFAULT 0,
  points INTEGER DEFAULT 0,
  UNIQUE(competition_id, team_id)
);

CREATE INDEX IF NOT EXISTS idx_matches_status_date ON matches(status, kickoff_at);
CREATE INDEX IF NOT EXISTS idx_matches_competition_date ON matches(competition_id, kickoff_at);
CREATE INDEX IF NOT EXISTS idx_events_match_minute ON match_events(match_id, minute);
CREATE INDEX IF NOT EXISTS idx_stats_match ON match_statistics(match_id);
CREATE INDEX IF NOT EXISTS idx_player_stats_match ON player_match_stats(match_id);
CREATE INDEX IF NOT EXISTS idx_standings_competition_position ON standings(competition_id, position);
