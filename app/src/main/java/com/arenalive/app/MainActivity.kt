package com.arenalive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arenalive.app.ui.ArenaColors
import com.arenalive.app.ui.ArenaTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArenaTheme {
                ArenaLiveApp()
            }
        }
    }
}

@Composable
private fun ArenaLiveApp() {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var selectedMatch by remember { mutableStateOf<Match?>(null) }
    var favoriteIds by remember { mutableStateOf(setOf("demo-1")) }
    var matches by remember { mutableStateOf(DemoData.matches) }
    var dataMode by remember { mutableStateOf("DADOS DEMONSTRATIVOS") }

    LaunchedEffect(BuildConfig.API_BASE_URL) {
        val remoteMatches = CloudflareApi.getLiveMatches(BuildConfig.API_BASE_URL, BuildConfig.API_LIVE_PATH)
        if (remoteMatches.isNotEmpty()) {
            matches = remoteMatches + DemoData.matches.filterNot { it.isLive }
            dataMode = "CLOUDFLARE"
        }
    }

    Scaffold(
        containerColor = ArenaColors.Background,
        bottomBar = {
            if (selectedMatch == null) {
                NavigationBar(
                    containerColor = ArenaColors.Surface,
                    contentColor = ArenaColors.Muted,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    MainTab.entries.forEach { tab ->
                        val icon = when (tab) {
                            MainTab.HOME -> Icons.Filled.Home
                            MainTab.GAMES -> Icons.Filled.CalendarMonth
                            MainTab.EXPLORE -> Icons.Filled.Explore
                            MainTab.FAVORITES -> Icons.Filled.Favorite
                        }
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ArenaColors.Lime,
                                selectedTextColor = ArenaColors.Lime,
                                indicatorColor = ArenaColors.SurfaceSoft,
                                unselectedIconColor = ArenaColors.Muted,
                                unselectedTextColor = ArenaColors.Muted
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (selectedMatch != null) {
                MatchDetailsScreen(
                    match = selectedMatch!!,
                    onBack = { selectedMatch = null }
                )
            } else {
                when (selectedTab) {
                    MainTab.HOME -> HomeScreen(
                        matches = matches,
                        favoriteIds = favoriteIds,
                        dataMode = dataMode,
                        onOpenMatch = { selectedMatch = it },
                        onToggleFavorite = { id ->
                            favoriteIds = if (favoriteIds.contains(id)) favoriteIds - id else favoriteIds + id
                        },
                        onGoToGames = { selectedTab = MainTab.GAMES }
                    )
                    MainTab.GAMES -> GamesScreen(matches, onOpenMatch = { selectedMatch = it })
                    MainTab.EXPLORE -> ExploreScreen()
                    MainTab.FAVORITES -> FavoritesScreen(
                        matches.filter { favoriteIds.contains(it.id) },
                        onOpenMatch = { selectedMatch = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    matches: List<Match>,
    favoriteIds: Set<String>,
    dataMode: String,
    onOpenMatch: (Match) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onGoToGames: () -> Unit
) {
    var sportFilter by remember { mutableStateOf("Todos") }
    val liveMatches = matches.filter { it.isLive }
    val filtered = liveMatches.filter { sportFilter == "Todos" || it.sport == sportFilter }
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM", Locale("pt", "BR")))
            .uppercase(Locale("pt", "BR"))
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        HeaderBar()
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("PLACARES AO VIVO", color = ArenaColors.Lime, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
                Text("A emoção não para.", color = ArenaColors.Text, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            }
            Surface(color = ArenaColors.SurfaceAlt, shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(ArenaColors.Lime, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(today, color = ArenaColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        DemoNotice(dataMode)
        Spacer(Modifier.height(18.dp))
        SectionHeading("Em andamento", "${liveMatches.size} partidas", showArrow = false)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos", "Futebol", "Basquete", "Tênis").forEach { item ->
                FilterChip(
                    selected = sportFilter == item,
                    onClick = { sportFilter = item },
                    label = { Text(item, fontSize = 12.sp) },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArenaColors.Lime,
                        selectedLabelColor = ArenaColors.Background,
                        containerColor = ArenaColors.Surface,
                        labelColor = ArenaColors.Muted
                    ),
                    border = null
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            EmptyState("Nenhuma partida nesta modalidade agora.")
        } else {
            filtered.forEach { match ->
                MatchCard(
                    match = match,
                    isFavorite = favoriteIds.contains(match.id),
                    onClick = { onOpenMatch(match) },
                    onFavorite = { onToggleFavorite(match.id) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        SectionHeading("Próximos jogos", "Ver calendário", onArrow = onGoToGames)
        Spacer(Modifier.height(12.dp))
        matches.filterNot { it.isLive }.take(2).forEach { match ->
            FixtureCard(match = match, onClick = { onOpenMatch(match) })
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(18.dp))
        FeatureBanner()
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun HeaderBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(ArenaColors.Lime),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.SportsSoccer, contentDescription = null, tint = ArenaColors.Background, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("ARENA", color = ArenaColors.Text, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, lineHeight = 18.sp)
            Text("LIVE", color = ArenaColors.Lime, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
        }
        Surface(color = ArenaColors.Surface, shape = CircleShape) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Search, contentDescription = "Busca disponível em Explorar", tint = ArenaColors.Text)
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(color = ArenaColors.Surface, shape = CircleShape) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.NotificationsNone, contentDescription = "Notificações", tint = ArenaColors.Text)
            }
        }
    }
}

@Composable
private fun DemoNotice(dataMode: String) {
    Surface(
        color = ArenaColors.SurfaceAlt,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArenaColors.Divider)
    ) {
        Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ArenaColors.Lime, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (dataMode == "CLOUDFLARE") "Conectado ao seu backend Cloudflare" else "Prévia do aplicativo · placares fictícios de demonstração",
                color = ArenaColors.Muted,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            Text(dataMode, color = ArenaColors.Lime, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    trailing: String,
    showArrow: Boolean = true,
    onArrow: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = ArenaColors.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        TextButton(onClick = { onArrow?.invoke() }, enabled = onArrow != null, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text(trailing, color = ArenaColors.Lime, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            if (showArrow) {
                Spacer(Modifier.width(3.dp))
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = ArenaColors.Lime, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun MatchCard(
    match: Match,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavorite: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArenaColors.Surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SportIcon(match.sport, 17.dp)
                Spacer(Modifier.width(7.dp))
                Text("${match.country}  ·  ${match.league}", color = ArenaColors.Muted, fontSize = 10.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Surface(color = if (match.isLive) Color(0xFF2B2022) else ArenaColors.SurfaceAlt, shape = RoundedCornerShape(6.dp)) {
                    Row(Modifier.padding(horizontal = 7.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (match.isLive) Box(Modifier.size(5.dp).background(ArenaColors.Red, CircleShape))
                        Text(if (match.isLive) " ${match.clock}" else match.startTime, color = if (match.isLive) ArenaColors.Red else ArenaColors.Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (onFavorite != null) {
                    IconButton(onClick = onFavorite, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.Star, contentDescription = "Favorito", tint = if (isFavorite) ArenaColors.Lime else ArenaColors.Muted, modifier = Modifier.size(17.dp))
                    }
                } else {
                    Icon(Icons.Filled.Star, contentDescription = "Favorito", tint = if (isFavorite) ArenaColors.Lime else ArenaColors.Muted, modifier = Modifier.padding(horizontal = 7.dp).size(17.dp))
                }
            }
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamLine(match.homeTeam, match.homeInitials, Modifier.weight(1f))
                ScoreBlock(match.homeScore, match.awayScore, match.isLive)
                TeamLine(match.awayTeam, match.awayInitials, Modifier.weight(1f), alignEnd = true)
            }
            Spacer(Modifier.height(11.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(ArenaColors.Divider))
            Spacer(Modifier.height(9.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(match.status, color = if (match.isLive) ArenaColors.Lime else ArenaColors.Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Spacer(Modifier.weight(1f))
                Text("Detalhes da partida", color = ArenaColors.Muted, fontSize = 9.sp)
                Spacer(Modifier.width(3.dp))
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = ArenaColors.Muted, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun FixtureCard(match: Match, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = ArenaColors.Surface,
        shape = RoundedCornerShape(15.dp)
    ) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(match.startTime, color = ArenaColors.Lime, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("AMANHÃ", color = ArenaColors.Muted, fontSize = 8.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(match.homeTeam, color = ArenaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                Text(match.awayTeam, color = ArenaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("VS", color = ArenaColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = ArenaColors.Muted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TeamLine(name: String, initials: String, modifier: Modifier, alignEnd: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start) {
        if (alignEnd) {
            Text(name, color = ArenaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(8.dp))
        }
        TeamBadge(initials)
        if (!alignEnd) {
            Spacer(Modifier.width(8.dp))
            Text(name, color = ArenaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TeamBadge(initials: String, large: Boolean = false) {
    Box(
        Modifier.size(if (large) 52.dp else 30.dp)
            .clip(CircleShape)
            .background(ArenaColors.SurfaceSoft)
            .border(1.dp, ArenaColors.Divider, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials.take(3), color = ArenaColors.Text, fontSize = if (large) 12.sp else 9.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ScoreBlock(home: String, away: String, isLive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(home, color = ArenaColors.Text, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
            Text("  :  ", color = ArenaColors.Muted, fontSize = 13.sp)
            Text(away, color = ArenaColors.Text, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
        }
        Text(if (isLive) "EM JOGO" else "", color = ArenaColors.Lime, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.9.sp)
    }
}

@Composable
private fun SportIcon(sport: String, size: androidx.compose.ui.unit.Dp) {
    val icon = when (sport) {
        "Basquete" -> Icons.Filled.SportsBasketball
        "Tênis" -> Icons.Filled.SportsTennis
        else -> Icons.Filled.SportsSoccer
    }
    Icon(icon, contentDescription = sport, tint = ArenaColors.Lime, modifier = Modifier.size(size))
}

@Composable
private fun FeatureBanner() {
    Surface(color = Color(0xFF263322), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ANÁLISE EM FOCO", color = ArenaColors.Lime, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(5.dp))
                Text("Cada jogo conta uma história.", color = ArenaColors.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Explore números, atletas e tendências.", color = Color(0xFFB4C0A7), fontSize = 10.sp)
            }
            Icon(Icons.Filled.BarChart, contentDescription = null, tint = ArenaColors.Lime, modifier = Modifier.size(35.dp))
        }
    }
}

@Composable
private fun GamesScreen(matches: List<Match>, onOpenMatch: (Match) -> Unit) {
    var day by remember { mutableStateOf("Hoje") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        HeaderBar()
        Spacer(Modifier.height(24.dp))
        Text("Calendário", color = ArenaColors.Text, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text("Acompanhe os próximos confrontos", color = ArenaColors.Muted, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Ontem", "Hoje", "Amanhã").forEach {
                FilterChip(
                    selected = day == it,
                    onClick = { day = it },
                    label = { Text(it, fontSize = 12.sp) },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArenaColors.Lime,
                        selectedLabelColor = ArenaColors.Background,
                        containerColor = ArenaColors.Surface,
                        labelColor = ArenaColors.Muted
                    ),
                    border = null
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        val dayMatches = when (day) {
            "Hoje" -> matches.filter { it.isLive }
            "Amanhã" -> matches.filterNot { it.isLive }
            else -> emptyList()
        }
        SectionHeading(if (day == "Hoje") "Partidas de hoje" else "Partidas · $day", "${dayMatches.size} no total", showArrow = false)
        Spacer(Modifier.height(12.dp))
        if (dayMatches.isEmpty()) {
            EmptyState("Nenhuma partida cadastrada para este dia na prévia.")
        } else {
            dayMatches.forEach { match ->
                MatchCard(match, false, { onOpenMatch(match) }, null)
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(25.dp))
    }
}

@Composable
private fun ExploreScreen() {
    var query by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        HeaderBar()
        Spacer(Modifier.height(22.dp))
        Text("Explore o esporte", color = ArenaColors.Text, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        Text("Competições, atletas e números", color = ArenaColors.Muted, fontSize = 12.sp)
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Filtrar competições demonstrativas", color = ArenaColors.Muted, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ArenaColors.Muted) },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ArenaColors.Lime,
                unfocusedBorderColor = ArenaColors.Divider,
                focusedTextColor = ArenaColors.Text,
                unfocusedTextColor = ArenaColors.Text,
                cursorColor = ArenaColors.Lime,
                focusedContainerColor = ArenaColors.Surface,
                unfocusedContainerColor = ArenaColors.Surface
            )
        )
        Spacer(Modifier.height(22.dp))
        SectionHeading("Competições em destaque", "Temporada 2026", showArrow = false)
        Spacer(Modifier.height(12.dp))
        val competitions = listOf(
            listOf("LN", "Liga Nacional", "Brasil", "Futebol", "20 equipes"),
            listOf("CC", "Copa Continental", "Internacional", "Futebol", "32 equipes"),
            listOf("LB", "Liga de Basquete", "Brasil", "Basquete", "16 equipes")
        ).filter { it[1].contains(query, ignoreCase = true) || it[2].contains(query, ignoreCase = true) || it[3].contains(query, ignoreCase = true) }
        if (competitions.isEmpty()) {
            EmptyState("Nenhuma competição demonstrativa corresponde à busca.")
        } else {
            competitions.forEachIndexed { index, item ->
                CompetitionCard(item[0], item[1], item[2], item[3], item[4])
                if (index != competitions.lastIndex) Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(22.dp))
        SectionHeading("Classificação", "Liga Nacional", showArrow = false)
        Spacer(Modifier.height(10.dp))
        StandingsCard()
        Spacer(Modifier.height(22.dp))
        SectionHeading("Comparação de atletas", "Análise", showArrow = false)
        Spacer(Modifier.height(10.dp))
        ComparisonCard()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CompetitionCard(initials: String, title: String, country: String, sport: String, teams: String) {
    Surface(color = ArenaColors.Surface, shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(ArenaColors.SurfaceSoft), contentAlignment = Alignment.Center) {
                Text(initials, color = ArenaColors.Lime, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = ArenaColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("$country · $sport · $teams", color = ArenaColors.Muted, fontSize = 10.sp)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = ArenaColors.Muted)
        }
    }
}

@Composable
private fun StandingsCard() {
    val rows = listOf(
        listOf("1", "Atlético Aurora", "72", "+28"),
        listOf("2", "União Central", "68", "+21"),
        listOf("3", "Racing Sul", "61", "+16"),
        listOf("4", "Estrela FC", "58", "+12")
    )
    Surface(color = ArenaColors.Surface, shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.padding(13.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 9.dp)) {
                Text("#", color = ArenaColors.Muted, fontSize = 9.sp, modifier = Modifier.width(22.dp))
                Text("EQUIPE", color = ArenaColors.Muted, fontSize = 9.sp, modifier = Modifier.weight(1f))
                Text("PTS", color = ArenaColors.Muted, fontSize = 9.sp, modifier = Modifier.width(34.dp))
                Text("SG", color = ArenaColors.Muted, fontSize = 9.sp, modifier = Modifier.width(30.dp))
            }
            rows.forEachIndexed { index, row ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(row[0], color = if (index == 0) ArenaColors.Lime else ArenaColors.Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp))
                    Text(row[1], color = ArenaColors.Text, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(row[2], color = ArenaColors.Text, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                    Text(row[3], color = ArenaColors.Lime, fontSize = 10.sp, modifier = Modifier.width(30.dp))
                }
                if (index != rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(ArenaColors.Divider))
            }
            Text("Tabela ilustrativa · dados demonstrativos", color = ArenaColors.Muted, fontSize = 9.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun ComparisonCard() {
    Surface(color = ArenaColors.Surface, shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerMini("CL", "Caio Lima", "Atacante", Modifier.weight(1f))
                Text("VS", color = ArenaColors.Lime, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp))
                PlayerMini("RN", "Rafael Nunes", "Atacante", Modifier.weight(1f))
            }
            Spacer(Modifier.height(13.dp))
            CompareMetric("Nota média", 8.4f, 8.1f)
            CompareMetric("Gols", 0.72f, 0.61f, leftLabel = "18", rightLabel = "15")
            CompareMetric("Assistências", 0.53f, 0.67f, leftLabel = "9", rightLabel = "11")
            Text("Exemplo visual de comparação · dados fictícios", color = ArenaColors.Muted, fontSize = 9.sp, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun PlayerMini(initials: String, name: String, role: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        TeamBadge(initials)
        Spacer(Modifier.width(7.dp))
        Column {
            Text(name, color = ArenaColors.Text, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(role, color = ArenaColors.Muted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun CompareMetric(name: String, left: Float, right: Float, leftLabel: String = "", rightLabel: String = "") {
    Column(Modifier.padding(top = 9.dp)) {
        Row {
            Text(leftLabel.ifBlank { String.format(Locale.US, "%.1f", left * 10) }, color = ArenaColors.Lime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(name, color = ArenaColors.Muted, fontSize = 9.sp)
            Spacer(Modifier.weight(1f))
            Text(rightLabel.ifBlank { String.format(Locale.US, "%.1f", right * 10) }, color = ArenaColors.Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(5.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.weight(left).height(5.dp).clip(CircleShape).background(ArenaColors.Lime))
            Box(Modifier.weight(right).height(5.dp).clip(CircleShape).background(ArenaColors.Blue))
        }
    }
}

@Composable
private fun FavoritesScreen(matches: List<Match>, onOpenMatch: (Match) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        HeaderBar()
        Spacer(Modifier.height(24.dp))
        Text("Seus favoritos", color = ArenaColors.Text, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        Text("Acompanhe seus jogos marcados", color = ArenaColors.Muted, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        if (matches.isEmpty()) {
            EmptyState("Você ainda não favoritou nenhuma partida. Toque na estrela de um jogo para encontrá-lo aqui.")
        } else {
            matches.forEach { match ->
                MatchCard(match, true, { onOpenMatch(match) }, null)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Surface(color = ArenaColors.Surface, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = ArenaColors.Lime, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(10.dp))
            Text(message, color = ArenaColors.Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MatchDetailsScreen(match: Match, onBack: () -> Unit) {
    var activeSection by remember { mutableStateOf("Resumo") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = ArenaColors.Surface, shape = CircleShape) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = ArenaColors.Text)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(match.league, color = ArenaColors.Text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(match.country, color = ArenaColors.Muted, fontSize = 10.sp)
            }
            Surface(color = if (match.isLive) Color(0xFF2B2022) else ArenaColors.SurfaceAlt, shape = RoundedCornerShape(7.dp)) {
                Text(if (match.isLive) "● ${match.clock}" else match.startTime, color = if (match.isLive) ArenaColors.Red else ArenaColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp))
            }
        }
        Spacer(Modifier.height(22.dp))
        Surface(color = ArenaColors.Surface, shape = RoundedCornerShape(21.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (match.isLive) "PARTIDA EM ANDAMENTO" else "PRÓXIMA PARTIDA", color = ArenaColors.Lime, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(17.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        TeamBadge(match.homeInitials, true)
                        Spacer(Modifier.height(8.dp))
                        Text(match.homeTeam, color = ArenaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${match.homeScore}  :  ${match.awayScore}", color = ArenaColors.Text, fontSize = 29.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text(match.status, color = ArenaColors.Lime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        TeamBadge(match.awayInitials, true)
                        Spacer(Modifier.height(8.dp))
                        Text(match.awayTeam, color = ArenaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Estádio Arena · dados de exemplo", color = ArenaColors.Muted, fontSize = 9.sp)
            }
        }
        Spacer(Modifier.height(17.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Resumo", "Estatísticas", "Escalações").forEach { section ->
                val selected = activeSection == section
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (selected) ArenaColors.Lime else ArenaColors.Surface)
                        .clickable { activeSection = section }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(section, color = if (selected) ArenaColors.Background else ArenaColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(17.dp))
        when (activeSection) {
            "Estatísticas" -> MatchStatisticsPanel()
            "Escalações" -> LineupsPanel(match)
            else -> MatchSummaryPanel()
        }
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun MatchSummaryPanel() {
    SectionHeading("Momentos da partida", "LINHA DO TEMPO", showArrow = false)
    Spacer(Modifier.height(12.dp))
    Surface(color = ArenaColors.Surface, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            DemoData.events.forEachIndexed { index, event ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(event.first, color = ArenaColors.Lime, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(38.dp))
                    Column(Modifier.weight(1f)) {
                        Text(event.second, color = ArenaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(event.third, color = ArenaColors.Muted, fontSize = 10.sp)
                    }
                    Text(when (event.second) { "Gol" -> "⚽"; "Cartão amarelo" -> "🟨"; else -> "↔" }, fontSize = 15.sp)
                }
                if (index != DemoData.events.lastIndex) {
                    Box(Modifier.padding(start = 38.dp, top = 10.dp, bottom = 10.dp).width(1.dp).height(17.dp).background(ArenaColors.Divider))
                }
            }
        }
    }
    Spacer(Modifier.height(18.dp))
    SectionHeading("Destaques", "NOTAS", showArrow = false)
    Spacer(Modifier.height(10.dp))
    Surface(color = ArenaColors.Surface, shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.padding(14.dp)) {
            PlayerRatingRow("CL", "Caio Lima", "Atlético Aurora", "8.7")
            Box(Modifier.fillMaxWidth().height(1.dp).background(ArenaColors.Divider).padding(vertical = 8.dp))
            PlayerRatingRow("RN", "Rafael Nunes", "União Central", "8.2")
            Text("Notas ilustrativas. O algoritmo de avaliação ainda precisa ser calibrado com dados licenciados.", color = ArenaColors.Muted, fontSize = 9.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun MatchStatisticsPanel() {
    SectionHeading("Estatísticas da partida", "DEMONSTRAÇÃO", showArrow = false)
    Spacer(Modifier.height(12.dp))
    Surface(color = ArenaColors.Surface, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ATLÉTICO AURORA", color = ArenaColors.Lime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("UNIÃO CENTRAL", color = ArenaColors.Blue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(13.dp))
            DemoData.footballStats.forEach { stat ->
                StatBar(stat)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
    Spacer(Modifier.height(17.dp))
    SectionHeading("Indicadores avançados", "EM DESENVOLVIMENTO", showArrow = false)
    Spacer(Modifier.height(9.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        MetricTile("xG", "1.84", "Aurora", Modifier.weight(1f))
        MetricTile("Ataques perigosos", "42", "Aurora", Modifier.weight(1f))
    }
    Spacer(Modifier.height(9.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        MetricTile("Grandes chances", "3", "Aurora", Modifier.weight(1f))
        MetricTile("Defesas", "4", "União", Modifier.weight(1f))
    }
    Text("Valores fictícios para demonstrar o layout. Mapas de calor e métricas reais dependem de cobertura da fonte de dados.", color = ArenaColors.Muted, fontSize = 9.sp, modifier = Modifier.padding(top = 10.dp))
}

@Composable
private fun StatBar(stat: MatchStatistic) {
    val total = (stat.home + stat.away).coerceAtLeast(1)
    val homeWeight = stat.home.toFloat() / total.toFloat()
    val homeText = if (stat.suffix == "%") "${stat.home}%" else stat.home.toString()
    val awayText = if (stat.suffix == "%") "${stat.away}%" else stat.away.toString()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(homeText, color = ArenaColors.Text, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(38.dp))
            Text(stat.name, color = ArenaColors.Muted, fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(awayText, color = ArenaColors.Text, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(38.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(ArenaColors.SurfaceSoft), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.weight(homeWeight.coerceAtLeast(0.06f)).fillMaxSize().background(ArenaColors.Lime))
            Box(Modifier.weight((1f - homeWeight).coerceAtLeast(0.06f)).fillMaxSize().background(ArenaColors.Blue))
        }
    }
}

@Composable
private fun MetricTile(title: String, value: String, footnote: String, modifier: Modifier) {
    Surface(modifier, color = ArenaColors.Surface, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(13.dp)) {
            Text(title, color = ArenaColors.Muted, fontSize = 9.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = ArenaColors.Text, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(footnote, color = ArenaColors.Lime, fontSize = 9.sp)
        }
    }
}

@Composable
private fun LineupsPanel(match: Match) {
    SectionHeading("Escalações", "FORMAÇÃO 4-3-3", showArrow = false)
    Spacer(Modifier.height(12.dp))
    Surface(color = Color(0xFF14251F), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(match.homeTeam.uppercase(), color = ArenaColors.Lime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(11.dp))
            PitchLineup()
            Spacer(Modifier.height(13.dp))
            Text(match.awayTeam.uppercase(), color = ArenaColors.Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Escalações demonstrativas · aguardando integração com a API esportiva", color = ArenaColors.Muted, fontSize = 9.sp)
        }
    }
    Spacer(Modifier.height(14.dp))
    SectionHeading("Banco de reservas", "JOGADORES", showArrow = false)
    Spacer(Modifier.height(9.dp))
    Surface(color = ArenaColors.Surface, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp)) {
            PlayerRatingRow("DR", "Davi Rocha", "Meio-campista", "—")
            PlayerRatingRow("BM", "Bruno Martins", "Atacante", "—")
            PlayerRatingRow("JS", "João Silva", "Defensor", "—")
        }
    }
}

@Composable
private fun PitchLineup() {
    Box(Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF1D3A2B))) {
        Canvas(Modifier.fillMaxSize().padding(9.dp)) {
            drawRect(color = Color(0xFF789B7E).copy(alpha = 0.35f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
            drawLine(Color(0xFF789B7E).copy(alpha = 0.35f), androidx.compose.ui.geometry.Offset(0f, size.height / 2), androidx.compose.ui.geometry.Offset(size.width, size.height / 2), 1.dp.toPx())
            drawCircle(Color(0xFF789B7E).copy(alpha = 0.35f), radius = size.minDimension * 0.13f, center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
        }
        Column(Modifier.fillMaxSize().padding(vertical = 9.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
            PitchPlayerRow(listOf("AA", "BL", "CR", "DM", "ER"))
            PitchPlayerRow(listOf("FN", "GL", "HR"))
            PitchPlayerRow(listOf("IL", "JM"))
            PitchPlayerRow(listOf("KL"))
        }
    }
}

@Composable
private fun PitchPlayerRow(players: List<String>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        players.forEach { initials ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(23.dp).clip(CircleShape).background(ArenaColors.Lime).border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(initials.take(2), color = ArenaColors.Background, fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
                Text(initials, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlayerRatingRow(initials: String, name: String, subtitle: String, rating: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        TeamBadge(initials)
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = ArenaColors.Text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = ArenaColors.Muted, fontSize = 9.sp)
        }
        Surface(color = Color(0xFF2B3B26), shape = RoundedCornerShape(7.dp)) {
            Text(rating, color = ArenaColors.Lime, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
        }
    }
}
