package com.prvz.simpletask.processing

import java.time.Duration

class TaskProcessingImpl(
    startEnabled: Boolean = true,
    parallelism: Int = 1,
    initialDelayInMillis: Long = 100,
    delayInMillis: Long = 100,
    shutdownWaitingMillis: Long = 100,
    private val restartPolicy: AfterCrashRestartPolicy? =
        AfterCrashRestartPolicy.Constant(5, Duration.ofMillis(100)),
    private val process: () -> Unit = { Thread.sleep(100) }
) : AbstractScheduledExecutorTaskProcessor(
    startEnabled = startEnabled,
    initialDelayInMillis = initialDelayInMillis,
    delayInMillis = delayInMillis,
    parallelism = parallelism,
    shutdownWaitingMillis = shutdownWaitingMillis
) {

    override fun restartPolicy(): AfterCrashRestartPolicy? = restartPolicy

    override fun process() = process.invoke()
}
