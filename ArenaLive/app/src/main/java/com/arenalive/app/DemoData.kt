package com.arenalive.app

object DemoData {
    // Estes resultados são fictícios e servem apenas para demonstrar a interface.
    val matches = listOf(
        Match("demo-1", "Futebol", "Liga Nacional", "Brasil", "Atlético Aurora", "União Central", "AA", "UC", "2", "1", "AO VIVO", "68'", "19:00", true),
        Match("demo-2", "Futebol", "Copa Continental", "Internacional", "Racing Sul", "Estrela FC", "RS", "EF", "0", "0", "AO VIVO", "32'", "19:30", true),
        Match("demo-3", "Basquete", "Liga de Basquete", "Brasil", "Rio Titans", "São Paulo Hoops", "RT", "SP", "78", "74", "AO VIVO", "Q4 · 04:21", "20:00", true),
        Match("demo-4", "Tênis", "Open Arena", "Internacional", "L. Almeida", "M. Costa", "LA", "MC", "2", "1", "AO VIVO", "3º set", "20:15", true),
        Match("demo-5", "Futebol", "Liga Nacional", "Brasil", "Porto Verde", "Vila Real", "PV", "VR", "—", "—", "AGENDADO", "Amanhã", "16:00", false),
        Match("demo-6", "Futebol", "Copa Continental", "Internacional", "Monte Azul", "Atlético do Norte", "MA", "AN", "—", "—", "AGENDADO", "Amanhã", "18:30", false)
    )

    val footballStats = listOf(
        MatchStatistic("Posse de bola", 56, 44),
        MatchStatistic("Finalizações", 14, 9, ""),
        MatchStatistic("No alvo", 6, 4, ""),
        MatchStatistic("Escanteios", 7, 3, ""),
        MatchStatistic("Passes certos", 87, 81),
        MatchStatistic("Grandes chances", 3, 2, "")
    )

    val events = listOf(
        Triple("12'", "Gol", "Atlético Aurora · Caio Lima"),
        Triple("34'", "Cartão amarelo", "União Central · Bruno Reis"),
        Triple("51'", "Gol", "União Central · Rafael Nunes"),
        Triple("63'", "Substituição", "Atlético Aurora · Davi entrou")
    )
}
