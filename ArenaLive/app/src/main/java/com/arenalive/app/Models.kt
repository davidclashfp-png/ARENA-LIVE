package com.arenalive.app

data class Match(
    val id: String,
    val sport: String,
    val league: String,
    val country: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeInitials: String,
    val awayInitials: String,
    val homeScore: String,
    val awayScore: String,
    val status: String,
    val clock: String,
    val startTime: String,
    val isLive: Boolean
)

data class MatchStatistic(
    val name: String,
    val home: Int,
    val away: Int,
    val suffix: String = "%"
)

enum class MainTab(val label: String) {
    HOME("Início"),
    GAMES("Jogos"),
    EXPLORE("Explorar"),
    FAVORITES("Favoritos")
}
