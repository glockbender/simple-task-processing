package com.prvz.simpletask.processing

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract class AbstractScheduledExecutorTaskProcessor(
    private val startEnabled: Boolean,
    private val parallelism: Int = DEFAULT_PARALLELISM,
    private val initialDelayInMillis: Long = DEFAULT_INITIAL_DELAY_MILLIS,
    private val delayInMillis: Long = DEFAULT_DELAY_MILLIS,
    private val shutdownWaitingMillis: Long = SHUTDOWN_WAITING_MILLIS
) : TaskProcessor {

    companion object {
        const val DEFAULT_INITIAL_DELAY_MILLIS = 1000L
        const val DEFAULT_DELAY_MILLIS = 100L
        const val SHUTDOWN_WAITING_MILLIS = 5000L
        const val DEFAULT_PARALLELISM = 1
    }

    private var executor = Executors.newScheduledThreadPool(parallelism)

    private var started: Boolean = false

    private var successfulProcessCount: Long = 0

    private var totalProcessCount: Long = 0

    override fun start(): Boolean =
        if (!startEnabled) {
            false
        } else {
            try {
                forceStart()
                true
            } catch (expected: Throwable) {
                false
            }
        }

    fun forceStart() {
        val isStopped = isStopped()
        if (isStopped) {
            executor = Executors.newScheduledThreadPool(parallelism)
        }
        if (!wasStarted() || isStopped) {
            executor.scheduleWithFixedDelay(
                this::innerProcess,
                initialDelayInMillis,
                delayInMillis,
                TimeUnit.MILLISECONDS
            )
        }
        started = true
    }

    override fun wasStarted(): Boolean = started

    override fun safeStop() {
        executor.shutdown()
        executor.awaitTermination(shutdownWaitingMillis, TimeUnit.MILLISECONDS)
        if (!isStopped()) {
            executor.shutdownNow()
        }
    }

    override fun successfulProcessCount(): Long = successfulProcessCount

    override fun totalProcessCount(): Long = totalProcessCount

    override fun isStopped() = executor.isShutdown

    private fun innerProcess() {
        totalProcessCount++
        try {
            process()
            successfulProcessCount++
        } catch (expected: Throwable) {
            if (restartPolicy() != null) {
                safeStop()
            }
        }
    }
}
