### 6) Entity target search & AI goal rewrites

#### 6.1 `NearestAttackableTargetGoal.findTarget(...)` - pooled consumer + player fast-path
Tpsum replaces `findTarget(...)` by injecting at `HEAD` and cancelling vanilla logic.

Changes:
- **Player fast-path:** if `targetType` is `Player` / `ServerPlayer`, call  
  `level.getNearestPlayer(targetConditions, mob, mob.getX(), mob.getEyeY(), mob.getZ())`.
- **Generic path:** otherwise iterate via the internal entity storage:
  `level.getEntities().get(EntityTypeTest.forClass(targetType), searchAABB, consumer)`
  using a ThreadLocal-pooled consumer (`NearestTargetSearch`).

**Benefit:** avoids building temporary lists and reduces the number of expensive `TargetingConditions.test(...)` calls.

#### 6.2 `NearestTargetSearch` - “distance-first” filtering for `TargetingConditions`
`NearestTargetSearch` is an `AbortableIterationConsumer<T>` stored in a `ThreadLocal` pool.

Per candidate:
1. compute `distSqr`
2. if `distSqr >= bestDistSqr`, skip immediately
3. only then run `conditions.test(getter, entity)` (visibility/raycast/team/etc.)

**Benefit:** heavy targeting checks run only for candidates that *improve* the current best.

#### 6.3 `AvoidEntityGoal.canUse(...)` - optimized nearest lookup + sticky target
Tpsum replaces `AvoidEntityGoal.canUse(...)` by injecting at `HEAD` and cancelling vanilla logic.

Changes:
- compute search AABB once: `mob.getBoundingBox().inflate(maxDist, 3.0D, maxDist)`
- choose `toAvoid` via:
    - **sticky fast-path:** if old `toAvoid` is alive and still within `maxDist`, keep it (no scan)
    - otherwise use `TLevelUtils.findNearestEntityOptimized(...)`
- keep vanilla-style safety check:
    - if `posAway == null` OR the “away” position is closer to `toAvoid` than the mob is → fail
- build path: `pathNav.createPath(posAway.x, posAway.y, posAway.z, 0)`

**Benefit:** avoids scanning every tick while fleeing, and reduces expensive predicate work during scans.

> [!NOTE]
> **Behavior nuance:**  
> The “sticky” `toAvoid` means a mob may keep fleeing the same entity even if a slightly closer avoid target appears, as long as the old one remains valid.

#### 6.4 `FollowFlockLeaderGoal.canUse(...)` - pooled flock scan (`FlockSearch`)
Tpsum replaces `FollowFlockLeaderGoal.canUse(...)` by injecting at `HEAD` and cancelling vanilla logic.

Changes:
- preserves early returns:
    - `hasFollowers()` → `false`
    - `isFollower()` → `true`
    - `nextStartTick` countdown gating
- scans nearby fish via:
  `level.getEntities().get(EntityTypeTest.forClass(AbstractSchoolingFish.class), AABB.inflate(8,8,8), consumer)`
  with a pooled consumer (`FlockSearch`) that collects:
    - first viable leader (`canBeFollowed()`)
    - followers list (`!fish.isFollower()`)

Then assigns followers to either the found leader or the current mob.

**Benefit:** avoids building temporary collections and reuses a pooled list for follower gathering.

#### 6.5 `FollowFlockLeaderGoal.nextStartTick(...)` - adjusted spread
Tpsum overwrites:
- `return 20 + fish.getRandom().nextInt(200);` (20..219 ticks)

**Benefit:** spreads flock reevaluation over time (helps reduce “many fish update on the same tick” spikes).

> [!NOTE]
> The comment in the mixin mentions a different range; the actual implementation is `20..219`.
