package com.prvz.simpletask.processing

import java.time.Clock
import java.time.Instant
import java.util.UUID

class ScheduledExecutorTaskWatchdog(
    processors: Collection<TaskProcessor>,
    delay: Long = DEFAULT_WATCHDOG_DELAY
) : AbstractScheduledExecutorTaskProcessor(
    startEnabled = true,
    parallelism = WATCHDOG_PARALLELISM,
    initialDelayInMillis = delay,
    delayInMillis = DEFAULT_WATCHDOG_DELAY
) {

    override fun restartPolicy(): AfterCrashRestartPolicy? = null

    var clock: Clock = Clock.systemUTC()

    private val idToProcessor: Map<UUID, TaskProcessor> = processors
        .filter { it.restartPolicy() != null }
        .associateBy { UUID.randomUUID() }

    private val idToNextStartTime: MutableMap<UUID, Instant?> = idToProcessor.keys
        .associateWith { Instant.now(clock) }
        .toMutableMap()

    companion object {

        const val DEFAULT_WATCHDOG_DELAY: Long = 1000

        const val WATCHDOG_PARALLELISM = 1
    }

    override fun process() {
        idToProcessor.forEach { (id, proc) ->
            if (proc.isStopped()) {
                aliveAndSetNextStartTime(id = id, proc = proc)
            }
        }
    }

    private fun aliveAndSetNextStartTime(
        id: UUID,
        proc: TaskProcessor
    ) {
        idToNextStartTime[id]
            ?.let { cachedNextStartTime ->
                val now = Instant.now(clock)
                if (cachedNextStartTime.isBefore(now)) {
                    val nextStartTime = proc.restartPolicy()!!.nextStartTime(now)
                    idToNextStartTime[id] = nextStartTime
                    if (nextStartTime != null) {
                        proc.start()
                    }
                }
            }
    }
}
