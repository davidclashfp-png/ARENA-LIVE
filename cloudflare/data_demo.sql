-- Conteúdo fictício para validar telas e rotas. Não representa jogos reais.
INSERT OR IGNORE INTO sports (id, name, icon) VALUES
('football', 'Futebol', 'football'),
('basketball', 'Basquete', 'basketball'),
('tennis', 'Tênis', 'tennis');

INSERT OR IGNORE INTO competitions (id, sport_id, name, country, season) VALUES
('league-national', 'football', 'Liga Nacional', 'Brasil', '2026'),
('continental-cup', 'football', 'Copa Continental', 'Internacional', '2026'),
('basket-league', 'basketball', 'Liga de Basquete', 'Brasil', '2026');

INSERT OR IGNORE INTO teams (id, sport_id, name, short_name, country) VALUES
('aurora', 'football', 'Atlético Aurora', 'Aurora', 'Brasil'),
('central', 'football', 'União Central', 'União', 'Brasil'),
('racing-sul', 'football', 'Racing Sul', 'Racing', 'Brasil'),
('estrela', 'football', 'Estrela FC', 'Estrela', 'Brasil'),
('rio-titans', 'basketball', 'Rio Titans', 'Titans', 'Brasil'),
('sp-hoops', 'basketball', 'São Paulo Hoops', 'SP Hoops', 'Brasil');

INSERT OR IGNORE INTO players (id, team_id, name, position, shirt_number) VALUES
('caio-lima', 'aurora', 'Caio Lima', 'Atacante', 9),
('rafael-nunes', 'central', 'Rafael Nunes', 'Atacante', 10),
('bruno-reis', 'central', 'Bruno Reis', 'Meio-campista', 8),
('davi-rocha', 'aurora', 'Davi Rocha', 'Meio-campista', 17);

INSERT OR IGNORE INTO matches (id, sport_id, competition_id, home_team_id, away_team_id, kickoff_at, status, clock, home_score, away_score, round_name, venue) VALUES
('demo-001', 'football', 'league-national', 'aurora', 'central', '2026-11-20T22:00:00Z', 'LIVE', '68', 2, 1, 'Rodada 35', 'Estádio Arena'),
('demo-002', 'football', 'continental-cup', 'racing-sul', 'estrela', '2026-11-20T22:30:00Z', 'LIVE', '32', 0, 0, 'Quartas de final', 'Arena Continental');

INSERT OR IGNORE INTO match_events (id, match_id, minute, event_type, team_id, player_id, description) VALUES
('event-001', 'demo-001', 12, 'GOAL', 'aurora', 'caio-lima', 'Gol de Caio Lima'),
('event-002', 'demo-001', 34, 'YELLOW_CARD', 'central', 'bruno-reis', 'Cartão amarelo para Bruno Reis'),
('event-003', 'demo-001', 51, 'GOAL', 'central', 'rafael-nunes', 'Gol de Rafael Nunes');

INSERT OR IGNORE INTO match_statistics (match_id, statistic_key, home_value, away_value, unit) VALUES
('demo-001', 'possession', 56, 44, '%'),
('demo-001', 'shots', 14, 9, ''),
('demo-001', 'shots_on_target', 6, 4, ''),
('demo-001', 'corners', 7, 3, ''),
('demo-001', 'pass_accuracy', 87, 81, '%'),
('demo-001', 'big_chances', 3, 2, '');

INSERT OR IGNORE INTO player_match_stats (match_id, player_id, rating, minutes_played, goals, assists, shots, key_passes) VALUES
('demo-001', 'caio-lima', 8.7, 68, 1, 0, 4, 2),
('demo-001', 'rafael-nunes', 8.2, 68, 1, 0, 3, 1);

INSERT OR IGNORE INTO standings (competition_id, team_id, position, played, won, drawn, lost, goals_for, goals_against, points) VALUES
('league-national', 'aurora', 1, 34, 22, 6, 6, 61, 33, 72),
('league-national', 'central', 2, 34, 20, 8, 6, 58, 37, 68),
('league-national', 'racing-sul', 3, 34, 18, 7, 9, 51, 35, 61),
('league-national', 'estrela', 4, 34, 17, 7, 10, 49, 37, 58);
