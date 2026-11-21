package com.arenalive.app.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arenalive.app.DemoData
import com.arenalive.app.Match

// Cores e pincéis do novo visual
object Arena {
    val Bg = Color(0xFF060A08)
    val Surface = Color(0xFF0F1612)
    val SurfaceHigh = Color(0xFF16201A)
    val SurfaceTop = Color(0xFF1F2C24)
    val Line = Color.White.copy(alpha = 0.08f)
    val Ink = Color(0xFFF3F8F4)
    val Muted = Color(0xFF93A59A)
    val Dim = Color(0xFF5F7166)
    val Lime = Color(0xFFC6F432)
    val LimeDeep = Color(0xFF86C61A)
    val Live = Color(0xFFFF4D5E)
    val Sky = Color(0xFF4DA8FF)
    val Gold = Color(0xFFFFC83D)
    val Pitch = Color(0xFF123222)
    val LimeBrush = Brush.linearGradient(listOf(Lime, LimeDeep))
    val CardBrush = Brush.verticalGradient(listOf(Color(0xFF141E18), Color(0xFF0E1511)))
}

fun sportIcon(sport: String): ImageVector = when (sport) {
    "Basquete" -> Icons.Filled.SportsBasketball
    "Tênis" -> Icons.Filled.SportsTennis
    else -> Icons.Filled.SportsSoccer
}

// Partida de exemplo (vem do DemoData) ou real (vem da API)
fun isDemoMatch(match: Match): Boolean = DemoData.matches.any { it.id == match.id }

// Cartão com cantos redondos, degradê leve e borda fina
fun Modifier.arenaCard(radius: Dp = 20.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return this.clip(shape).background(Arena.CardBrush).border(1.dp, Arena.Line, shape)
}

// Animação de entrada: aparece subindo, com atraso por posição
@Composable
fun Modifier.entrance(index: Int = 0, start: Boolean = true): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(start) {
        if (start) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 520,
                    delayMillis = (index * 80).coerceAtMost(640),
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 44f
    }
}

// Ponto pulsante de "ao vivo"
@Composable
fun LiveDot(modifier: Modifier = Modifier, color: Color = Arena.Live, size: Dp = 8.dp) {
    val transition = rememberInfiniteTransition(label = "liveDot")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )
    Box(modifier.size(size * 2.2f), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size * (1f + pulse * 1.2f))
                .alpha(0.7f * (1f - pulse))
                .background(color, CircleShape)
        )
        Box(Modifier.size(size).background(color, CircleShape))
    }
}

// Escudo com as iniciais do time
@Composable
fun Crest(initials: String, size: Dp = 36.dp, highlight: Boolean = false) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Arena.SurfaceTop, Arena.SurfaceHigh)))
            .border(1.dp, if (highlight) Arena.Lime.copy(alpha = 0.7f) else Arena.Line, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.take(3).uppercase(),
            color = Arena.Ink,
            fontSize = (size.value * 0.3f).sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun ArenaLogoMark(size: Dp = 40.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.34f)).background(Arena.LimeBrush),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.SportsSoccer,
            contentDescription = null,
            tint = Arena.Bg,
            modifier = Modifier.size(size * 0.62f)
        )
    }
}

@Composable
fun ArenaChip(label: String, selected: Boolean, onClick: () -> Unit, icon: ImageVector? = null) {
    val bg by animateColorAsState(
        targetValue = if (selected) Arena.Lime else Arena.Surface,
        animationSpec = tween(220),
        label = "chipBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Arena.Bg else Arena.Muted,
        animationSpec = tween(220),
        label = "chipFg"
    )
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, if (selected) Color.Transparent else Arena.Line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(18.dp).clip(CircleShape).background(Arena.Lime))
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            color = Arena.Ink,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        if (action != null) {
            Text(
                text = action,
                color = Arena.Lime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(enabled = onAction != null) { onAction?.invoke() }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color, live: Boolean = false, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (live) {
            LiveDot(color = color, size = 6.dp)
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun DemoTag() {
    Text(
        text = "EXEMPLO",
        color = Arena.Gold,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Arena.Gold.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

@Composable
fun EmptyPanel(message: String) {
    Column(
        Modifier.fillMaxWidth().arenaCard().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.SportsSoccer, contentDescription = null, tint = Arena.Lime, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(10.dp))
        Text(message, color = Arena.Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun FavoriteButton(active: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (active) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "favScale"
    )
    Box(
        Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = if (active) "Remover dos favoritos" else "Adicionar aos favoritos",
            tint = if (active) Arena.Lime else Arena.Muted.copy(alpha = 0.55f),
            modifier = Modifier.size(22.dp).graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
    }
}

// Bloco cinza que brilha enquanto os dados carregam
@Composable
fun ShimmerBlock(modifier: Modifier = Modifier, radius: Dp = 16.dp) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )
    val brush = Brush.linearGradient(
        colors = listOf(Arena.SurfaceHigh, Arena.SurfaceTop, Arena.SurfaceHigh),
        start = Offset(x * 1400f - 500f, 0f),
        end = Offset(x * 1400f, 260f)
    )
    Box(modifier.clip(RoundedCornerShape(radius)).background(brush))
}

// Procura a foto "hero_stadium" na pasta drawable. Se não existir, devolve null.
@Composable
fun rememberHeroPhoto(): Painter? {
    val context = LocalContext.current
    val resId = remember(context) {
        context.resources.getIdentifier("hero_stadium", "drawable", context.packageName)
    }
    return if (resId != 0) painterResource(id = resId) else null
}

// Faixa grande do topo: usa a foto se existir, senão desenha um fundo de estádio
@Composable
fun HeroBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val photo = rememberHeroPhoto()
    val shape = RoundedCornerShape(26.dp)
    Box(modifier.clip(shape).border(1.dp, Arena.Line, shape)) {
        if (photo != null) {
            Image(
                painter = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Canvas(Modifier.matchParentSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF1A3D1C), Color(0xFF0C1C13), Color(0xFF070D0A)),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                )
                val glowCenter = Offset(size.width * 0.88f, size.height * 0.05f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Arena.Lime.copy(alpha = 0.38f), Color.Transparent),
                        center = glowCenter,
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = glowCenter
                )
                val lineColor = Color.White.copy(alpha = 0.07f)
                val stroke = Stroke(width = 2.dp.toPx())
                drawCircle(
                    lineColor,
                    radius = size.height * 0.42f,
                    center = Offset(size.width * 0.8f, size.height * 0.7f),
                    style = stroke
                )
                drawLine(
                    lineColor,
                    Offset(size.width * 0.8f, 0f),
                    Offset(size.width * 0.8f, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(Color(0x55060A08), Color(0xEE060A08)))
            )
        )
        content()
    }
}
