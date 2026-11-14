package com.mustafadeveloper.colorclash.stroop

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Dynamic Theme Gradients
val DarkGameGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF2C0B4D), // Deep Violet
        Color(0xFF160F38), // Rich Indigo
        Color(0xFF090618)  // Midnight Dark
    )
)

val LightGameGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFEDE7F6), // Soft Lavender
        Color(0xFFF3E5F5), // Light Lilac
        Color(0xFFFAFAFA)  // Clean White
    )
)

@Composable
fun StroopGameScreen(
    viewModel: StroopViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()
    val isDark = state.isDarkMode

    val backgroundBrush = if (isDark) DarkGameGradient else LightGameGradient
    val cardBg = if (isDark) Color(0xDD1E1A36) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x33A370FF) else Color(0x227C4DFF)
    val mainTextColor = if (isDark) Color.White else Color(0xFF1B004B)
    val subTextColor = if (isDark) Color(0xFFD1C4E9) else Color(0xFF5E35B1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        if (!state.isPlaying && !state.isGameOver) {
            ModeSelectionScreen(
                isDark = isDark,
                cardBg = cardBg,
                cardBorder = cardBorder,
                mainTextColor = mainTextColor,
                subTextColor = subTextColor,
                onSelectMode = viewModel::startGame,
                onToggleTheme = viewModel::toggleDarkMode,
                onHowToPlay = { viewModel.setShowHowToPlay(true) },
                onPrivacyPolicy = { viewModel.setShowPrivacyPolicy(true) }
            )
        } else {
            InGamePlayScreen(
                state = state,
                isDark = isDark,
                cardBg = cardBg,
                cardBorder = cardBorder,
                mainTextColor = mainTextColor,
                subTextColor = subTextColor,
                onAnswerSelected = viewModel::submitAnswer,
                onQuit = viewModel::returnToMenu
            )
        }

        if (state.isGameOver) {
            GameOverDialog(
                state = state,
                onPlayAgain = { viewModel.startGame(state.mode) },
                onMainMenu = viewModel::returnToMenu
            )
        }

        if (state.showHowToPlayDialog) {
            HowToPlayDialog(
                onDismiss = { viewModel.setShowHowToPlay(false) }
            )
        }

        if (state.showPrivacyPolicyDialog) {
            PrivacyPolicyDialog(
                onDismiss = { viewModel.setShowPrivacyPolicy(false) }
            )
        }
    }
}

@Composable
private fun ModeSelectionScreen(
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    mainTextColor: Color,
    subTextColor: Color,
    onSelectMode: (GameMode) -> Unit,
    onToggleTheme: () -> Unit,
    onHowToPlay: () -> Unit,
    onPrivacyPolicy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Toolbar: Theme Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onToggleTheme,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cardBg,
                    contentColor = mainTextColor
                ),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Text(
                    text = if (isDark) "🌙 Dark Mode" else "☀️ Light Mode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Center Brand & Mode Cards
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚡ COLOR CLASH",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = mainTextColor
            )
            Text(
                text = "The Ultimate Stroop Focus Challenge",
                fontSize = 14.sp,
                color = subTextColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 36.dp)
            )

            ModeCard(
                title = GameMode.SURVIVAL.title,
                description = GameMode.SURVIVAL.description,
                iconText = "❤️",
                cardBg = cardBg,
                cardBorder = cardBorder,
                mainTextColor = mainTextColor,
                subTextColor = subTextColor,
                onClick = { onSelectMode(GameMode.SURVIVAL) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModeCard(
                title = GameMode.TIME_ATTACK.title,
                description = GameMode.TIME_ATTACK.description,
                iconText = "⏱️",
                cardBg = cardBg,
                cardBorder = cardBorder,
                mainTextColor = mainTextColor,
                subTextColor = subTextColor,
                onClick = { onSelectMode(GameMode.TIME_ATTACK) }
            )
        }

        // Bottom Menu Options: How to Play & Privacy Policy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onHowToPlay,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, cardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = mainTextColor)
            ) {
                Text(text = "❓ How to Play", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onPrivacyPolicy,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, cardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = mainTextColor)
            ) {
                Text(text = "🔒 Privacy Policy", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    description: String,
    iconText: String,
    cardBg: Color,
    cardBorder: Color,
    mainTextColor: Color,
    subTextColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8E52FF).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconText, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = mainTextColor
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = subTextColor
                )
            }

            Text(
                text = "PLAY",
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF8E52FF),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun InGamePlayScreen(
    state: StroopGameState,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    mainTextColor: Color,
    subTextColor: Color,
    onAnswerSelected: (ColorItem) -> Unit,
    onQuit: () -> Unit
) {
    val question = state.currentQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar (Score & Status)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "SCORE", fontSize = 11.sp, color = subTextColor, fontWeight = FontWeight.Bold)
                    Text(text = "${state.score}", fontSize = 26.sp, fontWeight = FontWeight.Black, color = mainTextColor)
                }

                if (state.streak >= 2) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF9800))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔥 ${state.streak}x Combo",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "BEST", fontSize = 11.sp, color = subTextColor, fontWeight = FontWeight.Bold)
                    Text(text = "${state.highScore}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = mainTextColor)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer Progress Bar
            val progress = (state.timeRemainingMs.toFloat() / state.totalTimeMs.toFloat()).coerceIn(0f, 1f)
            val progressColor by animateColorAsState(
                targetValue = when {
                    progress > 0.5f -> Color(0xFF00E676)
                    progress > 0.25f -> Color(0xFFFFD600)
                    else -> Color(0xFFFF1744)
                },
                animationSpec = tween(durationMillis = 200),
                label = "progress_color"
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = progressColor,
                trackColor = if (isDark) Color(0x331F1A3A) else Color(0x337C4DFF)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.mode == GameMode.SURVIVAL) {
                    Text(
                        text = "Lives: " + "❤️".repeat(state.lives.coerceAtLeast(0)),
                        fontSize = 14.sp
                    )
                } else {
                    val secLeft = (state.timeRemainingMs / 1000L).coerceAtLeast(0)
                    Text(
                        text = "⏱️ ${secLeft}s left",
                        fontSize = 14.sp,
                        color = mainTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Quit",
                    color = subTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onQuit)
                )
            }
        }

        // Center Target Card
        TargetQuestionCard(
            question = question,
            cardBg = cardBg,
            cardBorder = cardBorder,
            subTextColor = subTextColor,
            lastAnswerCorrect = state.lastAnswerCorrect
        )

        // Bottom Answer Options Grid
        AnswerOptionsGrid(
            options = question.options,
            cardBg = cardBg,
            cardBorder = cardBorder,
            mainTextColor = mainTextColor,
            onAnswerSelected = onAnswerSelected
        )
    }
}

@Composable
private fun TargetQuestionCard(
    question: StroopQuestion,
    cardBg: Color,
    cardBorder: Color,
    subTextColor: Color,
    lastAnswerCorrect: Boolean?
) {
    val borderColor by animateColorAsState(
        targetValue = when (lastAnswerCorrect) {
            true -> Color(0xFF00E676)
            false -> Color(0xFFFF1744)
            null -> cardBorder
        },
        animationSpec = tween(300),
        label = "border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .border(3.dp, borderColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Prompt Header Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF8E52FF))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    text = question.targetType.prompt,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            // Word with Conflict Color
            Text(
                text = question.wordItem.displayName,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = question.colorItem.composeColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = question.targetType.subtitle,
                fontSize = 12.sp,
                color = subTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnswerOptionsGrid(
    options: List<ColorItem>,
    cardBg: Color,
    cardBorder: Color,
    mainTextColor: Color,
    onAnswerSelected: (ColorItem) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OptionButton(
                item = options[0],
                cardBg = cardBg,
                cardBorder = cardBorder,
                mainTextColor = mainTextColor,
                modifier = Modifier.weight(1f),
                onClick = { onAnswerSelected(options[0]) }
            )
            OptionButton(
                item = options[1],
                cardBg = cardBg,
                cardBorder = cardBorder,
                mainTextColor = mainTextColor,
                modifier = Modifier.weight(1f),
                onClick = { onAnswerSelected(options[1]) }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OptionButton(
                item = options[2],
                cardBg = cardBg,
                cardBorder = cardBorder,
                mainTextColor = mainTextColor,
                modifier = Modifier.weight(1f),
                onClick = { onAnswerSelected(options[2]) }
            )
            OptionButton(
                item = options[3],
                cardBg = cardBg,
                cardBorder = cardBorder,
                mainTextColor = mainTextColor,
                modifier = Modifier.weight(1f),
                onClick = { onAnswerSelected(options[3]) }
            )
        }
    }
}

@Composable
private fun OptionButton(
    item: ColorItem,
    cardBg: Color,
    cardBorder: Color,
    mainTextColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(item.composeColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = item.displayName,
                color = mainTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun HowToPlayDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "❓ How to Play Color Clash",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Welcome to Color Clash! Test your brain's speed and cognitive focus.",
                    fontSize = 14.sp
                )

                Text(
                    text = "1. Read the Top Badge:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "• 'Match INK COLOR!' ➔ Tap the button for the color the word is drawn in.\n• 'Match WORD TEXT!' ➔ Tap the button matching the written text word.",
                    fontSize = 13.sp
                )

                Text(
                    text = "2. Beware the Stroop Effect!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Your brain automatically reads the text before recognizing the color. Don't let your eyes trick you!",
                    fontSize = 13.sp
                )

                Text(
                    text = "3. Combo Multipliers:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Answering consecutive questions correctly builds a 🔥 Combo Multiplier to boost your total score!",
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got It!")
            }
        }
    )
}

@Composable
private fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🔒 Privacy Policy",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Color Clash Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Your privacy is important to us. Color Clash is designed to be fully privacy-friendly:\n\n" +
                            "• No Personal Data Collected: We do not collect names, emails, contacts, or location data.\n" +
                            "• Offline & Local Storage: Game scores and settings (such as high score and dark/light mode preference) are stored locally on your device.\n" +
                            "• No Ad Tracking: We do not track your activity across other apps or websites.\n\n" +
                            "If you have any questions, enjoy playing Color Clash!",
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun GameOverDialog(
    state: StroopGameState,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val accuracy = if (state.totalQuestionsAnswered > 0) {
        (state.correctAnswersCount.toFloat() / state.totalQuestionsAnswered * 100).toInt()
    } else 0

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = "🎮 Game Over!",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Final Score: ${state.score}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "High Score: ${state.highScore}", color = Color.Gray)
                Text(text = "Max Combo Streak: 🔥 ${state.maxStreak}")
                Text(text = "Accuracy: $accuracy% (${state.correctAnswersCount}/${state.totalQuestionsAnswered})")
            }
        },
        confirmButton = {
            Button(onClick = onPlayAgain) {
                Text("Play Again")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onMainMenu) {
                Text("Main Menu")
            }
        }
    )
}
