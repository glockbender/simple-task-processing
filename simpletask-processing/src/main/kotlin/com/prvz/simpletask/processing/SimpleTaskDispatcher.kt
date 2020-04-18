package com.prvz.simpletask.processing

class SimpleTaskDispatcher(
    processors: Collection<TaskProcessor>,
    watchdogEnabled: Boolean
) {

    private val processors: Collection<TaskProcessor>

    private val watchdog: ScheduledExecutorTaskWatchdog?

    init {
        if (processors.any { it.wasStarted() }) throw IllegalStateException("Any of task processor is already started")
        this.processors = processors

        watchdog = if (watchdogEnabled)
            ScheduledExecutorTaskWatchdog(processors)
        else null
    }

    fun startAll() {
        processors.forEach { it.start() }
        watchdog?.start()
    }

    fun stopAll() {
        watchdog?.safeStop()
        processors.forEach { it.safeStop() }
    }
}
