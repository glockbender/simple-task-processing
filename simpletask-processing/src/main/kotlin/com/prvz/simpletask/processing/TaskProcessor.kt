package com.prvz.simpletask.processing

interface TaskProcessor {

    fun start(): Boolean

    fun wasStarted(): Boolean

    fun safeStop()

    fun process()

    fun successfulProcessCount(): Long

    fun totalProcessCount(): Long

    fun isStopped(): Boolean

    fun restartPolicy(): AfterCrashRestartPolicy?
}
