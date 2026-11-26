package com.inventsolutions.reflexrush.game

/**
 * Applies elapsed gameplay time to active targets without changing engine
 * state. Expired targets are returned separately so GameEngine can apply
 * mode-specific miss, shield, life, score, and event rules.
 */
class TargetLifecycle {
    fun advance(targets: List<GameTarget>, elapsedMs: Long): TargetLifecycleResult {
        require(elapsedMs >= 0L) { "Elapsed time cannot be negative." }

        val expired = targets.filter { it.remainingLifetimeMs <= elapsedMs }
        val remaining = targets
            .filter { it.remainingLifetimeMs > elapsedMs }
            .map { it.copy(remainingLifetimeMs = it.remainingLifetimeMs - elapsedMs) }

        return TargetLifecycleResult(
            remainingTargets = remaining,
            expiredTargets = expired
        )
    }
}

data class TargetLifecycleResult(
    val remainingTargets: List<GameTarget>,
    val expiredTargets: List<GameTarget>
)
