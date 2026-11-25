package com.inventsolutions.reflexrush.game

/**
 * Pure high-score rule, kept separate from Android storage for easy testing.
 */
object BestScore {
    fun afterRound(currentBest: Int, roundScore: Int): Int {
        require(currentBest >= 0) { "Current best score cannot be negative." }
        require(roundScore >= 0) { "Round score cannot be negative." }
        return maxOf(currentBest, roundScore)
    }
}
