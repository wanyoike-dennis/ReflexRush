# Reflex Rush

**Reflex Rush** is an Android reflex and reaction-time arcade game by Invent Solutions. The game challenges players to spot and tap positive targets quickly while avoiding dangerous targets. It is designed to be fast, easy to understand, satisfying to replay, and playable offline.

> **Project status:** The home screen, core gameplay engine, playable game screen, and local personal-best persistence are implemented and verified on a testing device. Phase 5 gameplay polish has been tested by the user. Phase 6 score-based difficulty progression is implemented remotely and awaiting local verification.

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

**Implementation status:** Score-based progression and the live level indicator are implemented on the feature branch. Local unit-test, Android build, and device verification are pending.

### Phase 7 — Testing and release readiness
- Run the complete unit-test suite and Android build.
- Install and play on an emulator or physical Android device.
- Check every difficulty transition, lifecycle behaviour, repeated play, and high-score persistence.
- Fix verified issues before considering V1 complete.

**Exit criteria:** Build succeeds, relevant tests pass, and the complete V1 flow has been manually exercised on Android.

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

## 8. Possible future enhancements (after V1)

Prioritise these only after the first release is stable:
- Difficulty levels or progressive difficulty.
- Additional modes, such as timed survival or accuracy challenge.
- Sound effects, music controls, and haptic feedback.
- Daily challenges and achievements.
- Detailed personal statistics and reaction-time summaries.
- Online leaderboards and optional cloud backup.
- Monetisation, only if a clear product decision is made.

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
| Phase 5 gameplay polish | Implemented remotely; local build and device verification pending |\n| Testing and release validation | Pending |

---

**Project:** Reflex Rush  
**Publisher / developer:** Invent Solutions  
**Repository:** [wanyoike-dennis/ReflexRush](https://github.com/wanyoike-dennis/ReflexRush)
