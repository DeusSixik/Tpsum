### 4) Task loop / scheduler (BlockableEventLoop)

Tpsum replaces parts of `BlockableEventLoop` task bookkeeping with:
- `MpscUnboundedArrayQueue` as the pending task queue
- `LongAdder` as the pending count
- streamlined `tell()` / `pollTask()` / `dropAllTasks()` / `waitForTasks()` logic
- `submitAsync()` uses `CompletableFuture.runAsync(..., this)`

**Benefit:** improved throughput when many tasks are posted concurrently.

**Compatibility warning:** this is one of the most mixin-heavy and mod-touched areas; conflicts with other task-loop / tick-loop / scheduler mods are more likely here.
