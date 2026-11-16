package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.allWords
import com.example.unscramble.data.SCORE_INCREASE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    var userGuess by mutableStateOf("")
        private set

    private lateinit var currentWord: String
    private val usedWords = mutableSetOf<String>()

    init {
        resetGame()
    }

    fun resetGame() {
        usedWords.clear()
        userGuess = ""
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            updateGameState(_uiState.value.score + SCORE_INCREASE)
        } else {
            _uiState.update { it.copy(isGuessedWordWrong = true) }
        }
        updateUserGuess("")
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {
        val nextWord = pickRandomWordAndShuffle()
        _uiState.update {
            it.copy(
                currentScrambledWord = nextWord,
                currentWordCount = it.currentWordCount + 1,
                score = updatedScore,
                isGuessedWordWrong = false
            )
        }
    }

    private fun pickRandomWordAndShuffle(): String {
        currentWord = (allWords - usedWords).random()
        usedWords.add(currentWord)
        return shuffleCurrentWord(currentWord)
    }

    private fun shuffleCurrentWord(word: String): String {
        val letters = word.toCharArray()
        do {
            letters.shuffle()
        } while (String(letters) == word)
        return String(letters)
    }
}
