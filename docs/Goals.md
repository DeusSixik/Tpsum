### 5) Goal flags & selector hot-path (Goal / Goal.Flag / GoalSelector)

#### 5.1 `Goal.Flag` - precomputed per-flag bit (`bts$getBits()`)
Tpsum injects into `Goal.Flag.<init>` and stores a precomputed bit:

- `bts$bits = 1 << (ordinal + 1)`
- exposed via `GoalFlagsBitsMaskGetter#bts$getBits()`

**Benefit:** each control flag becomes representable as a single integer bit (cheap to combine and test).

#### 5.2 `Goal.setFlags(...)` - cached goal flag bitmask (`bts$getBits()`)
Tpsum injects into `Goal.setFlags(...)` (at `RETURN`) and updates a cached mask:

- `bts$bits = GoalFlagsUtils.bitsFromEnum(flags)`
- exposed via `GoalBitsMaskSupport#bts$getBits()`

**Benefit:** avoids repeatedly re-building/iterating flag sets when checking a goal’s control flags.

#### 5.3 `GoalSelector.tick(...)` - redirect `goalContainsAnyFlags(...)` to bitmask check
Inside `GoalSelector.tick(...)`, the vanilla call:

- `goalContainsAnyFlags(WrappedGoal, EnumSet<Goal.Flag>)`

is redirected to:

- `bts$goalContainsAnyFlags(WrappedGoal, int disabledFlagsBits)`
- implementation: `(((GoalBitsMaskSupport) goal).bts$getBits() & disabledFlagsBits) != 0`

Tpsum also maintains a `@Unique int bts$disabledFlags` mask inside `GoalSelector`.

**Benefit:** replaces `EnumSet`-based checks with a single fast `& != 0` test in the hot tick loop.

#### 5.4 `GoalSelector.disableControlFlag / enableControlFlag` - bit operations
Tpsum overwrites:
- `disableControlFlag(flag)`: `bts$disabledFlags |= flagBits`
- `enableControlFlag(flag)`: `bts$disabledFlags &= ~flagBits`

**Benefit:** constant-time updates with no set mutations or iteration.

> [!NOTE]
> **Compatibility note:**  
> `GoalSelector` is a common mixin target. Mods that redirect/overwrite the same methods (`tick`, flag gating, etc.) may conflict.
