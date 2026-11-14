package com.mustafadeveloper.colorclash.stroop

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class ColorItem(val displayName: String, val composeColor: Color) {
    RED("RED", Color(0xFFE53935)),
    BLUE("BLUE", Color(0xFF1E88E5)),
    GREEN("GREEN", Color(0xFF43A047)),
    YELLOW("YELLOW", Color(0xFFFBC02D)),
    PURPLE("PURPLE", Color(0xFF8E24AA)),
    ORANGE("ORANGE", Color(0xFFFB8C00))
}

enum class TargetType(val prompt: String, val subtitle: String) {
    INK_COLOR("Match INK COLOR!", "Tap the color the word is written in"),
    WORD_TEXT("Match WORD TEXT!", "Tap the color named by the word")
}

enum class GameMode(val title: String, val description: String) {
    SURVIVAL("Classic Survival", "3 Lives • Race the timer"),
    TIME_ATTACK("30s Time Attack", "30 Seconds • Score as much as possible")
}

data class StroopQuestion(
    val wordItem: ColorItem,
    val colorItem: ColorItem,
    val targetType: TargetType,
    val options: List<ColorItem>
) {
    val correctAnswer: ColorItem
        get() = if (targetType == TargetType.INK_COLOR) colorItem else wordItem
}

data class StroopGameState(
    val mode: GameMode = GameMode.SURVIVAL,
    val currentQuestion: StroopQuestion? = null,
    val score: Int = 0,
    val highScore: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val lives: Int = 3,
    val timeRemainingMs: Long = 3000L,
    val totalTimeMs: Long = 3000L,
    val totalQuestionsAnswered: Int = 0,
    val correctAnswersCount: Int = 0,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val isDarkMode: Boolean = true,
    val showHowToPlayDialog: Boolean = false,
    val showPrivacyPolicyDialog: Boolean = false
)

class StroopQuestionGenerator {

    fun generateQuestion(currentScore: Int): StroopQuestion {
        val allColors = ColorItem.entries.toTypedArray()

        // Randomize word and ink color (ensure they are different for Stroop conflict effect)
        val wordItem = allColors.random()
        var colorItem = allColors.random()
        while (colorItem == wordItem) {
            colorItem = allColors.random()
        }

        // Target type: 60% chance INK_COLOR, 40% WORD_TEXT
        val targetType = if (Random.nextFloat() < 0.6f) TargetType.INK_COLOR else TargetType.WORD_TEXT

        // Select 4 option choices including the correct answer and trick choices
        val correctAnswer = if (targetType == TargetType.INK_COLOR) colorItem else wordItem
        val wrongAnswer = if (targetType == TargetType.INK_COLOR) wordItem else colorItem

        val remainingColors = allColors.filter { it != correctAnswer && it != wrongAnswer }.shuffled()
        val options = (listOf(correctAnswer, wrongAnswer) + remainingColors.take(2)).shuffled()

        return StroopQuestion(
            wordItem = wordItem,
            colorItem = colorItem,
            targetType = targetType,
            options = options
        )
    }

    fun calculateRoundTime(score: Int, mode: GameMode): Long {
        if (mode == GameMode.TIME_ATTACK) {
            return 30000L // 30 seconds total mode
        }
        // Survival mode: starts at 3000ms, decreases as score increases down to 1100ms
        val calculated = 3000L - (score * 45L)
        return maxOf(1100L, calculated)
    }
}
