package com.example.music.audio


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput


enum class RepeatMode {
    OFF,
    ALL,
    ONE
}
@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    name = "Spotify Music Player Preview"
)

@Composable
fun SpotifyMusicPlayerScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0.35f) }
    var isShuffleOn by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(RepeatMode.OFF) }

    val bgColor1 = Color(0xFF0A3A47)
    val bgColor2 = Color(0xFF051E26)
    val accentColor = Color(0xFF00D9FF)
    val textColor = Color.White
    val secondaryText = Color.White.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(bgColor1, bgColor2)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Back Button (Left corner)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close Player",
                        tint = textColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Central Content - Album Art with Title/Artist Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    // Album Image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                            .border(
                                width = 3.dp,
                                color = Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0B5A6B),
                                        Color(0xFF051E26)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://via.placeholder.com/400",
                            contentDescription = "Album Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Title and Artist OVER the image
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                            //verticalArrangement = Arrangement.spacedBy(50.dp)
                        ) {
                            Spacer(modifier = Modifier.height(90.dp))
                            Text(
                                text = "Titulo de la Cancion",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Artista",
                                fontSize = 16.sp,
                                color = secondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Play Button - Positioned outside clip area
                    Button(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 38.dp)
                            .size(76.dp)
                            .clip(RoundedCornerShape(50)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = bgColor2,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Control Buttons (Shuffle, Previous, Next, Repeat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isShuffleOn = !isShuffleOn },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleOn) accentColor else secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        repeatMode = when (repeatMode) {
                            RepeatMode.OFF -> RepeatMode.ALL
                            RepeatMode.ALL -> RepeatMode.ONE
                            RepeatMode.ONE -> RepeatMode.OFF
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            tint = when (repeatMode) {
                                RepeatMode.OFF -> secondaryText
                                else -> accentColor
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        if (repeatMode == RepeatMode.ONE) {
                            Text(
                                text = "1",
                                fontSize = 7.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }


            var currentPosition by remember { mutableStateOf(0.4f) }
            val accentColor = Color(0xFF00D9FF)
            val secondaryText = Color.White.copy(alpha = 0.6f)

            SpotifyProgressBar(
                progress = currentPosition,
                onProgressChange = { newValue -> currentPosition = newValue },
                accentColor = accentColor
            )

            /*
                        // Progress Bar (Spotify style - thin slider)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Slider(
                                value = currentPosition,
                                onValueChange = { currentPosition = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0:20",
                                    fontSize = 12.sp,
                                    color = secondaryText,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "3:35",
                                    fontSize = 12.sp,
                                    color = secondaryText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }*/

            // Album Info
            Text(
                text = "Album: Nombre del Album",
                fontSize = 13.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Pon un espacio fijo en su lugar
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            // Bottom Menu and Info (Right corner) with divider between them
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    horizontalAlignment = Alignment.End, // Cambiado a End
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.15f),
                        thickness = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

        }
    }
}
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyProgressBar1 (
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color

) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp), // algo más delgado
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            ),
            // Custom thumb para hacerlo más pequeño
            thumb = {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(accentColor, shape = RoundedCornerShape(50))
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "8:20",
                fontSize = 12.sp,
                color = secondaryText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "49:13",
                fontSize = 12.sp,
                color = secondaryText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyProgressBar(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp) // espacio para el track y thumb
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onProgressChange(newProgress)
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .height(4.dp) // grosor muy delgado
            ) {
                val centerY = size.height / 2f
                // Track inactivo
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Track activo
                drawLine(
                    color = accentColor,
                    start = Offset(0f, centerY),
                    end = Offset(size.width * progress, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Thumb (pequeño círculo)
                drawCircle(
                    color = accentColor,
                    radius = 6.dp.toPx(),
                    center = Offset(size.width * progress, centerY)
                )
            }
        }

        // Tiempo o etiqueta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "8:20",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "49:13",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


