package com.lavozapp.ui.radio

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.lavozapp.service.RadioForegroundService
import com.lavozapp.ui.theme.*

@Composable
fun RadioScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(0.8f) }

    // ExoPlayer for preview indicator only
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(p0: Boolean) { isPlaying = p0 }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(7.dp)) {
                    drawCircle(Color(LvpRed.value))
                }
                Spacer(Modifier.width(6.dp))
                Text("EN VIVO", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp, color = LvpRed)
            }
            Text("Pucón, Chile", fontSize = 9.sp, color = LvpGrayMuted)
        }

        Spacer(Modifier.height(32.dp))

        // Logo: RADIO · LA VOZ · DE PUCÓN
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(40.dp).height(1.dp).background(LvpRed))
            Spacer(Modifier.width(12.dp))
            Text("RADIO", fontFamily = FontFamily.SansSerif, fontSize = 14.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 5.sp, color = LvpRed)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.width(40.dp).height(1.dp).background(LvpRed))
        }

        Text("LA VOZ", fontFamily = FontFamily.Serif, fontSize = 64.sp,
            fontWeight = FontWeight.Black, color = LvpRed)

        Text("DE PUCÓN", fontFamily = FontFamily.SansSerif, fontSize = 22.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 6.sp, color = LvpDark)

        Spacer(Modifier.height(12.dp))

        // Waveform decoration
        val infiniteTransition = rememberInfiniteTransition()
        val heights = listOf(16f, 28f, 12f, 36f, 8f, 24f, 14f)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            heights.forEachIndexed { i, h ->
                val animatedHeight by infiniteTransition.animateFloat(
                    initialValue = h - 8f, targetValue = h + 8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = i * 80),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Box(Modifier.width(4.dp).height(animatedHeight.dp).clip(RoundedCornerShape(2.dp)).background(LvpRed.copy(alpha = 0.5f)))
            }
        }

        Spacer(Modifier.height(32.dp))

        // Player card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column {
                // Video placeholder
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp).background(LvpSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📺 La Voz de Pucón", fontSize = 16.sp, color = LvpGray500)
                }

                // Controls
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Animated waveform bars
                        val barHeights = listOf(6f, 18f, 10f, 24f, 14f, 20f, 8f)
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            barHeights.forEachIndexed { i, h ->
                                val animH by infiniteTransition.animateFloat(
                                    initialValue = 3f, targetValue = h,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, delayMillis = i * 80),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )
                                Box(Modifier.width(3.dp).height(if (isPlaying) animH.dp else 4.dp)
                                    .clip(RoundedCornerShape(1.dp)).background(LvpRed))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Al aire ahora", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp, color = LvpRed)
                            Text("Radio La Voz", fontSize = 16.sp, fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold, color = LvpDark)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Play button
                    Button(
                        onClick = {
                            val intent = Intent(context, RadioForegroundService::class.java).apply {
                                action = if (isPlaying) RadioForegroundService.ACTION_PAUSE
                                else RadioForegroundService.ACTION_PLAY
                            }
                            context.startForegroundService(intent)
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LvpRed)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isPlaying) "Pausar" else "Escuchar en vivo",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))

                    // Volume
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null,
                            tint = LvpGray500, modifier = Modifier.size(16.dp))
                        Slider(
                            value = volume,
                            onValueChange = { volume = it; exoPlayer.volume = it },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = LvpRed, activeTrackColor = LvpRed)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tagline
        Text("INFORMAMOS · CONECTAMOS · ACOMPAÑAMOS",
            fontSize = 9.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 2.5.sp, color = LvpGrayMuted)
        Text("La radio que se ve y se escucha desde Pucón",
            fontSize = 12.sp, color = LvpGray500)
    }
}
