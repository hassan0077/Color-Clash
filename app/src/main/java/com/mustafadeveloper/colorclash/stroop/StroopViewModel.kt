package com.mustafadeveloper.colorclash.stroop

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StroopViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("stroop_clash_prefs", Context.MODE_PRIVATE)
    private val generator = StroopQuestionGenerator()

    private val savedDarkMode = prefs.getBoolean("IS_DARK_MODE", true)

    private val _gameState = MutableStateFlow(StroopGameState(isDarkMode = savedDarkMode))
    val gameState: StateFlow<StroopGameState> = _gameState.asStateFlow()

    private var timerJob: Job? = null

    fun toggleDarkMode() {
        val newMode = !_gameState.value.isDarkMode
        prefs.edit { putBoolean("IS_DARK_MODE", newMode) }
        _gameState.value = _gameState.value.copy(isDarkMode = newMode)
    }

    fun setShowHowToPlay(show: Boolean) {
        _gameState.value = _gameState.value.copy(showHowToPlayDialog = show)
    }

    fun setShowPrivacyPolicy(show: Boolean) {
        _gameState.value = _gameState.value.copy(showPrivacyPolicyDialog = show)
    }

    fun startGame(mode: GameMode) {
        timerJob?.cancel()
        val highScore = getHighScoreForMode(mode)
        val firstQuestion = generator.generateQuestion(0)
        val initialTimeMs = generator.calculateRoundTime(0, mode)

        _gameState.value = _gameState.value.copy(
            mode = mode,
            currentQuestion = firstQuestion,
            score = 0,
            highScore = highScore,
            streak = 0,
            maxStreak = 0,
            lives = if (mode == GameMode.SURVIVAL) 3 else 1,
            timeRemainingMs = initialTimeMs,
            totalTimeMs = initialTimeMs,
            totalQuestionsAnswered = 0,
            correctAnswersCount = 0,
            isPlaying = true,
            isGameOver = false,
            lastAnswerCorrect = null,
            showHowToPlayDialog = false,
            showPrivacyPolicyDialog = false
        )

        startTimer()
    }

    fun submitAnswer(selectedColor: ColorItem) {
        val currentState = _gameState.value
        if (!currentState.isPlaying || currentState.isGameOver) return

        val question = currentState.currentQuestion ?: return
        val isCorrect = selectedColor == question.correctAnswer

        val newStreak = if (isCorrect) currentState.streak + 1 else 0
        val newMaxStreak = maxOf(currentState.maxStreak, newStreak)
        val pointsToAdd = if (isCorrect) 100 + (newStreak * 25) else 0
        val newScore = currentState.score + pointsToAdd

        val newTotalAnswered = currentState.totalQuestionsAnswered + 1
        val newCorrectCount = currentState.correctAnswersCount + (if (isCorrect) 1 else 0)

        val newHighScore = maxOf(currentState.highScore, newScore)
        if (newHighScore > getHighScoreForMode(currentState.mode)) {
            saveHighScoreForMode(currentState.mode, newHighScore)
        }

        if (currentState.mode == GameMode.SURVIVAL) {
            val newLives = if (isCorrect) currentState.lives else currentState.lives - 1
            if (newLives <= 0) {
                endGame(newScore, newHighScore, newMaxStreak, newTotalAnswered, newCorrectCount)
                return
            }

            // Next question in Survival
            val nextQuestion = generator.generateQuestion(newScore)
            val nextTimeMs = generator.calculateRoundTime(newScore, GameMode.SURVIVAL)

            _gameState.value = currentState.copy(
                currentQuestion = nextQuestion,
                score = newScore,
                highScore = newHighScore,
                streak = newStreak,
                maxStreak = newMaxStreak,
                lives = newLives,
                timeRemainingMs = nextTimeMs,
                totalTimeMs = nextTimeMs,
                totalQuestionsAnswered = newTotalAnswered,
                correctAnswersCount = newCorrectCount,
                lastAnswerCorrect = isCorrect
            )

            restartTimerForQuestion(nextTimeMs)
        } else {
            // TIME_ATTACK mode (timer continues uninterrupted)
            val nextQuestion = generator.generateQuestion(newScore)
            _gameState.value = currentState.copy(
                currentQuestion = nextQuestion,
                score = newScore,
                highScore = newHighScore,
                streak = newStreak,
                maxStreak = newMaxStreak,
                totalQuestionsAnswered = newTotalAnswered,
                correctAnswersCount = newCorrectCount,
                lastAnswerCorrect = isCorrect
            )
        }
    }

    fun returnToMenu() {
        timerJob?.cancel()
        val currentDarkMode = _gameState.value.isDarkMode
        _gameState.value = StroopGameState(isDarkMode = currentDarkMode)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val intervalMs = 20L
            while (isActive) {
                delay(intervalMs)
                val current = _gameState.value
                if (!current.isPlaying || current.isGameOver) break

                val remaining = current.timeRemainingMs - intervalMs
                if (remaining <= 0) {
                    handleTimeExpired()
                    break
                } else {
                    _gameState.value = current.copy(timeRemainingMs = remaining)
                }
            }
        }
    }

    private fun restartTimerForQuestion(timeMs: Long) {
        timerJob?.cancel()
        _gameState.value = _gameState.value.copy(
            timeRemainingMs = timeMs,
            totalTimeMs = timeMs
        )
        startTimer()
    }

    private fun handleTimeExpired() {
        val current = _gameState.value
        if (current.mode == GameMode.SURVIVAL) {
            val newLives = current.lives - 1
            if (newLives <= 0) {
                endGame(current.score, current.highScore, current.maxStreak, current.totalQuestionsAnswered, current.correctAnswersCount)
            } else {
                // Deduct life and go to next question
                val nextQuestion = generator.generateQuestion(current.score)
                val nextTimeMs = generator.calculateRoundTime(current.score, GameMode.SURVIVAL)
                _gameState.value = current.copy(
                    currentQuestion = nextQuestion,
                    streak = 0,
                    lives = newLives,
                    timeRemainingMs = nextTimeMs,
                    totalTimeMs = nextTimeMs,
                    lastAnswerCorrect = false
                )
                restartTimerForQuestion(nextTimeMs)
            }
        } else {
            // Time attack expired -> Game Over
            endGame(current.score, current.highScore, current.maxStreak, current.totalQuestionsAnswered, current.correctAnswersCount)
        }
    }

    private fun endGame(
        finalScore: Int,
        finalHighScore: Int,
        maxStreak: Int,
        totalAnswered: Int,
        correctCount: Int
    ) {
        timerJob?.cancel()
        _gameState.value = _gameState.value.copy(
            score = finalScore,
            highScore = finalHighScore,
            maxStreak = maxStreak,
            totalQuestionsAnswered = totalAnswered,
            correctAnswersCount = correctCount,
            isPlaying = false,
            isGameOver = true,
            timeRemainingMs = 0L
        )
    }

    private fun getHighScoreForMode(mode: GameMode): Int {
        return prefs.getInt("HIGH_SCORE_${mode.name}", 0)
    }

    private fun saveHighScoreForMode(mode: GameMode, score: Int) {
        prefs.edit { putInt("HIGH_SCORE_${mode.name}", score) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
