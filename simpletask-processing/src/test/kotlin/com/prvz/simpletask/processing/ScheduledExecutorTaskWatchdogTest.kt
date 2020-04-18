package com.prvz.simpletask.processing

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

class ScheduledExecutorTaskWatchdogTest {

    @Test
    fun `should restart processor`() {

        var first = true

        val processor = TaskProcessingImpl {
            if (first) {
                first = false
                throw RuntimeException()
            }
        }

        val watchdog = ScheduledExecutorTaskWatchdog(processors = listOf(processor), delay = 100)
        watchdog.start()

        processor.start()

        Thread.sleep(1500)

        Assertions.assertTrue(!processor.isStopped())
    }

    @Test
    fun `should not restart processor after max attempts`() {

        var cnt = 0
        val max = 3

        val processor = TaskProcessingImpl(
            restartPolicy = AfterCrashRestartPolicy.Constant(1, Duration.ofMillis(100))
        ) {
            if (cnt++ < max) {
                throw RuntimeException()
            }
        }

        val watchdog = ScheduledExecutorTaskWatchdog(processors = listOf(processor), delay = 100)
        watchdog.start()

        processor.start()

        Thread.sleep(1500)

        Assertions.assertTrue(processor.isStopped())
    }
}
