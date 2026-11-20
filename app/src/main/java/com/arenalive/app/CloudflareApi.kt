package com.arenalive.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Busca placares no Worker. Sem URL configurada, o app permanece em modo demonstrativo. */
object CloudflareApi {
    suspend fun getLiveMatches(baseUrl: String, livePath: String): List<Match> = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext emptyList()

        var connection: HttpURLConnection? = null
        try {
            val endpoint = baseUrl.trimEnd('/') + "/" + livePath.trimStart('/')
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5_000
                readTimeout = 7_000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode !in 200..299) return@withContext emptyList()
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val array = root.optJSONArray("matches") ?: return@withContext emptyList()
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    add(
                        Match(
                            id = item.optString("id", "remote-$i"),
                            sport = item.optString("sport", "Futebol"),
                            league = item.optString("league", "Competição"),
                            country = item.optString("country", ""),
                            homeTeam = item.optString("homeTeam", "Mandante"),
                            awayTeam = item.optString("awayTeam", "Visitante"),
                            homeInitials = item.optString("homeInitials", "M"),
                            awayInitials = item.optString("awayInitials", "V"),
                            homeScore = item.optString("homeScore", "0"),
                            awayScore = item.optString("awayScore", "0"),
                            status = item.optString("status", "AO VIVO"),
                            clock = item.optString("clock", "—"),
                            startTime = item.optString("startTime", "—"),
                            isLive = item.optBoolean("isLive", true)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection?.disconnect()
        }
    }
}
