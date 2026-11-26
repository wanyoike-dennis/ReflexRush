package com.inventsolutions.reflexrush.game

/**
 * Resolves combo state transitions and their domain events without owning
 * the broader game state or target lifecycle.
 */
data class ComboTransition(
    val comboCount: Int,
    val multiplier: Int,
    val events: List<GameplayEvent>
)

class ComboTransitionResolver(
    private val rules: ComboRules
) {
    fun afterGoodHit(previousCount: Int, previousMultiplier: Int): ComboTransition {
        require(previousCount >= 0) { "Previous combo count cannot be negative." }
        require(previousMultiplier >= 1) { "Previous multiplier must be at least one." }

        val nextCount = previousCount + 1
        val nextMultiplier = rules.multiplierFor(nextCount)
        val events = buildList {
            if (nextCount != previousCount) {
                add(
                    GameplayEvent.ComboChanged(
                        previousCount = previousCount,
                        newCount = nextCount,
                        multiplier = nextMultiplier
                    )
                )
            }
            if (nextMultiplier > previousMultiplier) {
                add(GameplayEvent.ComboMilestone(nextCount, nextMultiplier))
            }
        }
        return ComboTransition(nextCount, nextMultiplier, events)
    }

    fun breakCombo(previousCount: Int, previousMultiplier: Int): ComboTransition {
        require(previousCount >= 0) { "Previous combo count cannot be negative." }
        require(previousMultiplier >= 1) { "Previous multiplier must be at least one." }

        if (previousCount == 0) {
            return ComboTransition(previousCount, previousMultiplier, emptyList())
        }

        return ComboTransition(
            comboCount = 0,
            multiplier = 1,
            events = listOf(
                GameplayEvent.ComboChanged(
                    previousCount = previousCount,
                    newCount = 0,
                    multiplier = 1
                )
            )
        )
    }
}
