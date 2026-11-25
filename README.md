# Reflex Rush

**Reflex Rush** is an Android reflex and reaction-time arcade game by Invent Solutions. The game challenges players to spot and tap positive targets quickly while avoiding dangerous targets. It is designed to be fast, easy to understand, satisfying to replay, and playable offline.

> **Project status:** The home screen, core gameplay engine, playable game screen, local personal-best persistence, gameplay polish, and Phase 6 score-based difficulty progression have been implemented and user-verified on a testing device. Phase 7 is focused on final regression testing and release readiness.

## 1. Project objectives

### Primary objective
Build a polished, responsive Android game that tests and improves a player's reaction speed, accuracy, and focus through short, repeatable rounds.

### Product objectives
- Make the game immediately understandable, with a simple start-to-play experience.
- Reward fast and accurate target selection.
- Create meaningful risk by penalising missed targets and dangerous-target taps.
- Make each round engaging through changing target positions and pacing.
- Encourage replay through personal best scores and progressively greater challenge.
- Keep the core game available without an internet connection.
- Deliver a clean, accessible interface that works across supported Android screen sizes.

### Engineering objectives
- Use Kotlin and Jetpack Compose, following the existing Android project structure.
- Keep gameplay rules separate from UI rendering where practical.
- Use lifecycle-aware state and predictable game-state transitions.
- Persist the player's high score locally.
- Keep the initial implementation lightweight; avoid unnecessary libraries, services, accounts, or backend dependencies.
- Add tests for core game rules and verify changes with Android builds and on-device play.

## 2. Target platform and technology

| Area | Initial decision |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI | Jetpack Compose |
| Application ID | `com.inventsolutions.reflexrush` |
| Minimum Android version | API 24 (as configured in the Android project) |
| Data storage | Local device storage for the initial release |
| Connectivity | Offline-first; no account or backend required for core play |

Existing Gradle, Kotlin, Compose, and Android configuration should be retained unless a demonstrated implementation requirement makes a change necessary.

## 3. Core gameplay concept

Targets appear at changing positions in the play area and remain visible for a limited lifetime.

- **Good targets:** Tap these before they disappear to earn points.
- **Danger targets:** Avoid tapping these. A tap subtracts points.
- **Missed targets:** A good target that disappears without being tapped costs one life. An expired danger target has no penalty.
- **Score:** A successful good-target hit adds points; a danger-target hit subtracts points, but the score cannot go below zero.
- **Lives:** The round ends when the player runs out of lives.
- **High score:** The best score is saved locally and shown on the home/game-over experience.

The initial, configurable gameplay defaults are:

| Rule | Initial value |
|---|---:|
| Starting lives | 3 |
| Good-target hit | +10 points |
| Danger-target hit | −10 points |
| Target lifetime | 1,400 ms |
| Spawn interval | 750 ms |
| Maximum active targets | 3 |
| Danger-target probability | 22% |

These are starting values for playtesting and may be adjusted in `GameConfig` without changing the game rules.

## 4. Version 1 scope (first playable release)

The first release should stay focused on one complete, reliable game loop.

### Included in V1
1. **Home screen**
   - Reflex Rush identity and short instructions.
   - Clear Play button.
   - Best score display.
2. **Gameplay screen**
   - Visible score and remaining lives.
   - A play area with targets appearing at changing positions.
   - Distinct visual treatment for good and danger targets.
   - Responsive tap targets and clear feedback.
3. **Gameplay rules**
   - Award points for valid good-target taps.
   - Remove tapped targets so they cannot score more than once.
   - Deduct a life when a good target expires.
   - Apply a penalty when a danger target is tapped.
   - End the round when lives reach zero.
4. **Game-over screen**
   - Final score and best score.
   - Clear Play Again action.
   - Return to home action if needed.
5. **Local persistence**
   - Save the best score on the device.
   - Load it when the app is reopened.
6. **Basic quality**
   - Handle leaving and returning to the app without creating duplicate game loops or timers.
   - Provide readable contrast, sensible touch areas, and layouts for different screen sizes.

### Explicitly out of scope for V1
- Online multiplayer, leaderboards, accounts, and cloud synchronisation.
- Ads, in-app purchases, and payment integration.
- Backend APIs.
- Daily challenges, achievements, and social sharing.
- Multiple game modes and extensive difficulty selection.
- Advanced analytics.

These can be considered after the core game is stable and tested.

## 5. Implementation plan

Work through these phases in order. Each phase should be reviewed and verified before moving to the next.

### Phase 0 — Project baseline and documentation
- Preserve the Android Studio starter configuration.
- Document product goals, gameplay rules, scope, and acceptance criteria.
- Confirm the feature branch is the working branch.

**Exit criteria:** Project plan is committed and the existing starter project remains intact.

### Phase 1 — UI foundation and visual direction
- Establish a consistent game visual style, colours, typography, spacing, and reusable Compose elements.
- Build the home screen and navigation between home, gameplay, and game-over states.
- Ensure the layout adapts to small and large screens.

**Exit criteria:** Screens render correctly and navigation works; gameplay can still be a placeholder at this stage.

### Phase 2 — Core gameplay engine
- Define game state, target types, score, lives, and configurable gameplay values.
- Implement random target spawning, safe play-area positions, target expiry, and tap handling.
- Enforce the maximum number of active targets.
- Prevent duplicate scoring and ignore invalid state updates.
- Add unit tests for scoring, penalties, missed targets, lives, round completion, and target limits.

**Exit criteria:** The core rules pass unit tests independently of screen styling. **Verified:** all 10 game-engine unit tests passed locally.

### Phase 3 — Connect gameplay to the UI
- Render active targets in the play area.
- Connect target taps and expiry events to the game state.
- Update score and lives immediately.
- Run one lifecycle-aware game loop and pause/resume it safely.
- Transition to game-over at the correct time.
- Provide replay and return-to-home actions.

**Exit criteria:** A complete round can be played from start to finish without manual intervention. **Verified:** local Android build, installation, and manual gameplay checks passed on a testing device.

### Phase 4 — High-score persistence and replay
- Store the best score locally using Android SharedPreferences, without adding a dependency.
- Load the saved best score on app launch and show it on the home screen.
- Update it only when a completed round beats the existing personal best.
- Show the best score and a new-personal-best message on the game-over screen.
- Keep replay and return-to-home flows with clean state resets.

**Verified:** Local SharedPreferences storage, home-screen display, game-over personal-best display, and persistence after closing/reopening the app were tested successfully on the user's device.

### Phase 5 — Feedback, polish, and accessibility
- Add restrained target entrance animations and a more noticeable game-over presentation.
- Show brief, colour-coded feedback for successful hits, danger penalties, and missed-life events.
- Provide Android vibrator feedback for gameplay actions.
- Review typography, contrast, touch target sizes, and layout behaviour.

**Verified by user:** Target animations, score/penalty/life feedback, vibration feedback, and the improved game-over transition were tested on the user's device.

### Phase 6 — Progressive difficulty
- Introduce five score-based difficulty levels, beginning at Level 1.
- Reduce the target spawn interval gradually as the score increases.
- Shorten new targets' visible lifetime at higher levels.
- Increase danger-target probability gradually, with a hard cap to keep the game fair.
- Show the current difficulty level during gameplay.
- Keep scoring, lives, target types, and the maximum active-target limit unchanged.
- Add unit tests for score thresholds, timing progression, danger probability, and level updates.

**Verified by user:** Five-level score-based progression, faster spawning, shorter target lifetimes, danger-target probability increases, and the live level indicator were pulled, built, and tested on the user's device.

### Phase 7 — Testing and release readiness
- Expand deterministic game-engine regression coverage for timing, simultaneous misses, and replay resets.
- Run debug and release unit-test tasks, then assemble both debug and release APKs.
- Install the debug build and exercise the complete V1 flow on a physical device or emulator.
- Verify all five difficulty levels, pause/resume, repeated rounds, game-over actions, and high-score persistence.
- Check compact and larger screen layouts, including portrait orientation and system navigation insets.
- Review app identity, launcher icon, backup behaviour, version metadata, and release signing configuration.
- Fix only confirmed defects; do not change dependencies or unrelated project configuration as part of release checks.

**Exit criteria:** Debug and release builds succeed, automated tests pass, the V1 acceptance checklist is completed on Android, and a signed release artifact can be produced using a private release keystore.

## 6. V1 acceptance checklist

- [ ] App opens to a finished home screen.
- [ ] Player can start a new round.
- [ ] Good targets appear in the play area.
- [ ] Tapping a good target awards points exactly once.
- [ ] Good targets that expire reduce lives.
- [ ] Danger targets are visually distinguishable.
- [ ] Tapping a danger target applies the defined penalty.
- [ ] The round ends when lives are exhausted.
- [ ] Game-over shows the final score.
- [ ] A new personal best is saved locally.
- [ ] Saved best score is available after restarting the app.
- [ ] Replay resets score, lives, targets, and timers correctly.
- [ ] Leaving and returning to the app does not create duplicate timers or gameplay loops.
- [ ] Core gameplay works without an internet connection.
- [ ] Unit tests and Android build pass, and manual device testing is completed.

## 7. Design and gameplay principles

- **Clarity first:** Players should understand what to tap and what to avoid without lengthy instructions.
- **Fast feedback:** Every action should have an immediate, understandable response.
- **Fair difficulty:** Challenge should increase through deliberate tuning, not unpredictable or impossible target placement.
- **Consistency:** Colours, shapes, animation, and feedback should communicate the same meaning throughout the game.
- **Performance:** Gameplay should remain smooth and responsive on supported devices.
- **Maintainability:** Keep game rules, persistence, and UI responsibilities clearly organised.
- **Offline by default:** No network connection should be needed to start or complete a round.

## 8. Hybrid gameplay expansion roadmap

After the current V1 regression and release-readiness work is complete, evolve Reflex Rush into a **hybrid arcade, competitive, and progression-driven game**. Preserve the verified Classic experience while adding the following features incrementally and in this order:

1. Combos
2. Powerups
3. Sound and audio settings
4. Game modes
5. Player statistics
6. Missions, achievements, and progression

The implementation must be incremental: complete and verify each stage before starting the next. Do not attempt to deliver all features in one large change.

### 8.1 Architectural foundation — before adding gameplay features

Before implementing combos, establish the engine contracts that later features will share. This is a small, deliberate foundation step, not a rewrite of the working game.

- Introduce an explicit `GameMode` model and a `GameSessionConfig` (or equivalent) so each session receives its rules and configuration. Initially, only Classic needs to be playable; the model must allow additional modes without duplicating the engine.
- Define typed, immutable `GameplayEvent` values for meaningful outcomes such as good-target hit, danger-target hit, target missed, life lost, combo milestone, powerup collected, level changed, and round completed.
- Make scoring an explicit engine responsibility through a central score/rules model. The engine should calculate awarded points, penalties, multipliers, and final session results consistently.
- Expose events and a final session summary to the app layer. Statistics, missions, achievements, audio, and UI feedback should react to these events rather than independently infer what happened from Compose state or score changes.
- Keep game rules pure Kotlin and independent of Android, Compose, audio APIs, and persistence. The UI renders engine state; storage records summaries and progress; audio responds to events.
- Keep the current Classic defaults and five-level difficulty progression unchanged unless a separately reviewed balancing change is required.
- Add deterministic tests for event emission, score calculation, session completion, and the Classic compatibility path.

**Architecture acceptance criteria:** Existing Classic gameplay and high-score behaviour remain intact; the engine can be configured for a future mode; gameplay events and round summaries are testable without Android or Compose; no feature-specific storage or UI logic is embedded in the engine.

### 8.2 Stage 0 — Complete the current release-readiness baseline

- Finish the pending Phase 7 regression tests and build checks.
- Confirm current gameplay, high-score persistence, difficulty progression, replay, and lifecycle pause/resume behaviour.
- Record the passing test and build baseline before extending the engine.

**Progress:** The existing `testDebugUnitTest` baseline passed locally before the architecture work. The remaining Phase 7 release/build/device checks are still pending.

**Exit criteria:** Existing feature branch is clean, current regression checks pass, and the verified Classic game remains a stable reference point.

### 8.3 Stage 1 — Engine foundation and gameplay events

- Add the mode/session configuration, typed gameplay events, central scoring rules, and completed-session summary described above.
- Adapt the current Classic flow to use these contracts without changing its visible rules.
- Keep the current high-score store working; do not migrate persistence unnecessarily.
- Add unit tests that prove existing Classic scoring, lives, difficulty, replay, and high-score integration remain compatible.

**Implementation pushed; local verification pending.** Added `GameMode` and `GameSessionConfig`, central `ScoreRules`, typed `GameplayEvent` values, and immutable `GameSessionSummary`. The Classic engine now emits and buffers events for round start, target hits, misses, life loss, level changes, and round completion. The gameplay screen consumes hit/life events for its existing feedback, and personal-best saving reads the completed session summary. Existing `GameConfig` construction remains supported for current tests and callers. Eight focused architecture tests were added.

**Exit criteria:** Classic still behaves as before, while the engine provides the shared contracts required by combos, modes, statistics, and missions.

### 8.4 Stage 2 — Combo system

- Count consecutive successful good-target hits.
- Use the initial multiplier thresholds: 1–2 hits = 1×, 3–4 = 2×, 5–9 = 3×, and 10+ = 5×.
- Reset the combo when a good target is missed or a danger target is tapped.
- Apply multipliers centrally in the engine's score calculation.
- Emit combo and combo-milestone events for UI feedback, statistics, and missions.
- Display the current streak and multiplier with clear, short animations.
- Track the best combo for the completed session.

**Exit criteria:** Multiplier boundaries, combo resets, score floors, event emission, and replay resets pass deterministic unit tests; the feature is verified in a local Android build and on a device.

### 8.5 Stage 3 — Powerups and special targets

Initial powerups:

| Powerup | Initial effect |
|---|---|
| Gold star | Award +50 bonus points |
| Double Points | Double eligible points for 5 seconds |
| Slow Time | Temporarily slow target expiry and spawning for 5 seconds |
| Shield | Prevent one missed-good-target life loss |

Implementation requirements:

- Model powerup target types and effects explicitly; do not overload ordinary good/danger target logic.
- Make spawn probability, effect duration, and effect limits configurable.
- Initially allow only one timed effect at a time; a shield may be held until consumed.
- Emit events when a powerup appears, is collected, activates, expires, or is consumed.
- Let each mode's configuration enable or disable individual powerups.
- Show active effects and remaining duration clearly in the gameplay UI.

**Exit criteria:** Collection, activation, expiration, timing, shield consumption, score interaction, mode restrictions, and event behaviour are unit-tested and device-verified.

### 8.6 Stage 4 — Sound and audio settings

- Add a small Android audio manager outside the game engine.
- Use short sound effects for target hits, danger penalties, combo milestones, powerup collection, life loss, game over, and new personal best.
- Add independent sound-effects and background-music on/off settings.
- Persist audio preferences locally.
- Respect app lifecycle transitions and release or pause audio resources appropriately.
- Start with sound effects; introduce optional background music only after effects and lifecycle handling are reliable.

**Exit criteria:** Audio controls persist across app restarts, sounds correspond to gameplay events, and audio does not continue unexpectedly when the app is backgrounded.

### 8.7 Stage 5 — Multiple game modes

Implement four modes through shared engine rules and mode-specific session configuration, not separate game engines:

| Mode | Initial rules |
|---|---|
| Classic | Preserve the current three-life game and progressive difficulty; support combos and configured powerups |
| Time Attack | 60-second round; maximise score before time expires |
| Survival | Three starting lives; survive escalating difficulty and maximise score |
| Precision | Fixed set of 30 targets; reward accuracy and penalise danger hits and misses |

- Add a mode-selection screen while keeping Classic easy to start.
- Define mode-specific end conditions, scoring configuration, difficulty behaviour, and powerup availability.
- Save personal-best scores separately for each mode.
- Include the selected mode in every session summary and gameplay event context.
- Ensure pausing, resuming, replaying, and returning home work for every mode.

**Exit criteria:** Each mode has distinct, tested rules and independent records; Classic remains compatible with the original experience.

### 8.8 Stage 6 — Player statistics

Track session and lifetime statistics from gameplay events and completed-session summaries:

- Games played and total play time.
- Targets hit, danger targets hit, and targets missed.
- Accuracy, using a documented and consistent denominator based on actual target outcomes (never derive accuracy from score).
- Highest score and highest level.
- Best combo and powerups collected.
- Per-mode best scores and records.

- Add a Statistics screen with overview, records, gameplay, and per-mode sections.
- Update persistent statistics at appropriate session/event boundaries, not as a side effect of Compose recomposition.
- Keep statistics available offline and across app restarts.

**Exit criteria:** Statistics reconcile with known test sessions, survive app restarts, and remain separated by game mode where appropriate.

### 8.9 Stage 7 — Missions, achievements, and progression

Initial mission examples:

| Mission | Completion condition |
|---|---|
| First Strike | Hit 20 good targets |
| Combo Master | Reach a 5-hit combo |
| Danger Dodger | Complete a round without tapping a danger target |
| Speed Demon | Score 250 in Time Attack |
| Survivor | Reach Level 4 in Classic |
| Power Collector | Collect 5 powerups |

- Track mission progress from typed gameplay events and session summaries.
- Add daily missions that refresh by the device's local calendar date.
- Persist progress and completion offline; completed missions must not silently reset.
- Add achievements for longer-term milestones.
- Introduce XP and cosmetic rewards, such as target colours or background themes, after mission tracking is reliable.
- Keep rewards cosmetic initially; do not create pay-to-win or gameplay advantages.
- Make date handling testable, including day changes and time-zone changes.

**Exit criteria:** Mission progress reflects actual gameplay, daily refresh is deterministic and tested, completed rewards persist, and the feature works offline.

### 8.10 Hybrid expansion quality checklist

- [ ] Existing Classic gameplay remains playable and recognisable.
- [ ] The engine owns scoring, mode rules, and gameplay event generation.
- [ ] UI, audio, statistics, and missions consume shared gameplay events rather than duplicating game logic.
- [ ] Each feature is implemented and verified before the next begins.
- [ ] Game modes have separate rules and personal-best records.
- [ ] Statistics and missions persist across app restarts.
- [ ] All gameplay features work offline.
- [ ] Unit tests cover engine rules, event sequences, scoring, timing, and progression.
- [ ] Debug/release builds and physical-device checks pass after meaningful milestones.
- [ ] No unnecessary dependencies, backend services, accounts, or unrelated refactors are introduced.

Online leaderboards, multiplayer, cloud synchronisation, monetisation, and social features remain outside this roadmap until the offline hybrid game is stable and there is a separate product decision to add them.

## 9. Development workflow

- Work on `feature/reflex-rush-game` until a change is reviewed and ready to merge.
- Inspect the current branch and relevant source files before editing.
- Keep changes focused on the agreed phase; avoid unrelated refactors.
- Do not change Gradle or dependency versions unless required and justified.
- Commit coherent milestones with descriptive messages.
- Pull the remote changes locally, then build and test in Android Studio.
- Report exactly what was changed and what was or was not verified.
- Update this README when scope, decisions, or phase status materially changes.

## 10. Current project status

| Item | Status |
|---|---|
| Android Studio starter project | Created |
| GitHub repository | Created |
| Feature branch `feature/reflex-rush-game` | Created |
| Project objectives and implementation plan | Documented in this README |
| Phase 1 home screen | Implemented and installed on a testing device |
| Phase 2 core gameplay engine | Implemented; all 10 unit tests passed locally |
| Gameplay screen integration | Implemented; build, installation, and gameplay verified on a testing device |
| High-score persistence | Implemented and verified on a testing device |
| Phase 5 gameplay polish | User tested; feedback improvements confirmed |
| Phase 6 progressive difficulty | Implemented and user-verified on a testing device |
| Phase 7 regression and release validation | In progress; the existing `testDebugUnitTest` baseline passed locally; release/build/device checks remain pending |\n| Hybrid expansion Stage 1 | Implemented and pushed; eight new architecture tests are awaiting local execution |\n| Hybrid expansion roadmap | Defined; architecture-first implementation underway |

---

**Project:** Reflex Rush  
**Publisher / developer:** Invent Solutions  
**Repository:** [wanyoike-dennis/ReflexRush](https://github.com/wanyoike-dennis/ReflexRush)
