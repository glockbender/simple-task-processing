package com.prvz.simpletask.processing

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AbstractScheduledExecutorTaskProcessorTest {

    @Test
    fun `should start when startEnabled = true`() {
        val processor = TaskProcessingImpl()
        processor.start()

        Assertions.assertTrue(processor.wasStarted())
    }

    @Test
    fun `should not start when startEnabled = false`() {
        val processor = TaskProcessingImpl(false)
        processor.start()

        Assertions.assertTrue(!processor.wasStarted())
    }

    @Test
    fun `process should successful execute at least once`() {
        val processor = TaskProcessingImpl()
        processor.start()

        Thread.sleep(500)

        Assertions.assertTrue(processor.successfulProcessCount() > 1)
    }

    @Test
    fun `process should execute at least once`() {
        val processor = TaskProcessingImpl()
        processor.start()

        Thread.sleep(500)

        Assertions.assertTrue(processor.totalProcessCount() > 1)
    }

    @Test
    fun `process should execute at least once but not successful`() {
        val processor = TaskProcessingImpl() { throw RuntimeException() }
        processor.start()

        Thread.sleep(500)

        Assertions.assertTrue(processor.totalProcessCount() == 1L)
        Assertions.assertTrue(processor.successfulProcessCount() == 0L)
    }

    @Test
    fun `should stop`() {

        val processor = TaskProcessingImpl()
        processor.start()

        processor.safeStop()

        Thread.sleep(500)

        Assertions.assertTrue(processor.isStopped())
    }

    @Test
    fun `should force start when started = false`() {
        val processor = TaskProcessingImpl(false)
        processor.forceStart()

        Assertions.assertTrue(processor.wasStarted())
    }

    @Test
    fun `should dead on exception while process running when hasn't restart policy`() {
        val processor = TaskProcessingImpl(
            process = { throw RuntimeException() })
        processor.start()

        Thread.sleep(500)

        Assertions.assertTrue(processor.isStopped())
    }

    @Test
    fun `should not dead on exception while process running when has restart policy`() {
        println(Thread.currentThread().name)
        val processor = TaskProcessingImpl(
            restartPolicy = null,
            process = { throw RuntimeException() })
        processor.start()

        Thread.sleep(500)

        Assertions.assertTrue(!processor.isStopped())
    }
}
