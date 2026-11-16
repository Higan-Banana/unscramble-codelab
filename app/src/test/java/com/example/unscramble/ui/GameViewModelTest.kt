package com.example.unscramble.ui

import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import org.junit.Assert.*
import org.junit.Test

class GameViewModelTest {
    @Test
    fun newGameStartsWithAScrambledWordAndZeroPoints() {
        val state = GameViewModel().uiState.value
        assertEquals(1, state.currentWordCount)
        assertEquals(0, state.score)
        assertFalse(state.isGameOver)
        assertFalse(state.isGuessedWordWrong)
        assertTrue(allWords.any {
            it != state.currentScrambledWord && letters(it) == letters(state.currentScrambledWord)
        })
    }

    @Test
    fun wrongAnswerAllowsRetryWithoutAdvancing() {
        val game = GameViewModel()
        val before = game.uiState.value
        game.updateUserGuess("respuesta incorrecta")
        game.checkUserGuess()
        assertEquals(before.copy(isGuessedWordWrong = true), game.uiState.value)
        assertEquals("", game.userGuess)
        solveCurrentWord(game)
        assertEquals(SCORE_INCREASE, game.uiState.value.score)
        assertEquals(2, game.uiState.value.currentWordCount)
        assertFalse(game.uiState.value.isGuessedWordWrong)
    }

    @Test
    fun skippingClearsErrorAndInputWithoutAddingPoints() {
        val game = GameViewModel()
        game.checkUserGuess()
        game.updateUserGuess("pendiente")
        game.skipWord()
        assertEquals(0, game.uiState.value.score)
        assertEquals(2, game.uiState.value.currentWordCount)
        assertFalse(game.uiState.value.isGuessedWordWrong)
        assertEquals("", game.userGuess)
    }

    @Test
    fun tenCorrectAnswersEndGameWithoutRepeatingWords() {
        val game = GameViewModel()
        val answeredWords = mutableSetOf<String>()
        repeat(MAX_NO_OF_WORDS) { round ->
            assertFalse(game.uiState.value.isGameOver)
            assertEquals(round + 1, game.uiState.value.currentWordCount)
            assertTrue(answeredWords.add(solveCurrentWord(game)))
            assertEquals((round + 1) * SCORE_INCREASE, game.uiState.value.score)
        }
        assertTrue(game.uiState.value.isGameOver)
        assertEquals(MAX_NO_OF_WORDS, game.uiState.value.currentWordCount)
        val finalState = game.uiState.value
        game.skipWord()
        game.checkUserGuess()
        assertEquals(finalState, game.uiState.value)
    }

    @Test
    fun tenSkipsEndGameAndResetClearsEverything() {
        val game = GameViewModel()
        repeat(MAX_NO_OF_WORDS) { game.skipWord() }
        assertTrue(game.uiState.value.isGameOver)
        assertEquals(0, game.uiState.value.score)
        game.updateUserGuess("pendiente")
        game.resetGame()
        assertEquals(1, game.uiState.value.currentWordCount)
        assertEquals(0, game.uiState.value.score)
        assertFalse(game.uiState.value.isGameOver)
        assertFalse(game.uiState.value.isGuessedWordWrong)
        assertEquals("", game.userGuess)
        repeat(MAX_NO_OF_WORDS) { solveCurrentWord(game) }
        assertEquals(MAX_NO_OF_WORDS * SCORE_INCREASE, game.uiState.value.score)
    }

    // Algunas palabras comparten letras: probamos las candidatas del diccionario real.
    private fun solveCurrentWord(game: GameViewModel): String {
        val before = game.uiState.value
        val candidates = allWords.filter { letters(it) == letters(before.currentScrambledWord) }
        for (word in candidates) {
            game.updateUserGuess(word.uppercase())
            game.checkUserGuess()
            if (game.uiState.value.score > before.score) return word
        }
        error("No se pudo resolver la palabra: ${before.currentScrambledWord}")
    }

    private fun letters(word: String) = word.toList().sorted()
}
