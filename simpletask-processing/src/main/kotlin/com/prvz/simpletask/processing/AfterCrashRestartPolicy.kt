package com.prvz.simpletask.processing

import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

sealed class AfterCrashRestartPolicy(
    maxAttempts: Int,
    baseWaitingDuration: Duration,
    attemptAndBaseDurationToFinalDuration: (Int, Duration) -> Duration
) {
    val maxAttempts: Int
    val baseWaitingDuration: Duration
    private val attemptAndBaseDurationToFinalDuration: (Int, Duration) -> Duration

    private var attempt: Int = 0

    var clock: Clock = Clock.systemUTC()

    companion object {
        const val DEFAULT_MAX_ATTEMPTS = 5
        val DEFAULT_DURATION = Duration.ofSeconds(5)
    }

    init {
        if (maxAttempts < 1)
            throw IllegalArgumentException("maxAttempts value must be positive")
        if (baseWaitingDuration.isNegative || baseWaitingDuration.isZero)
            throw IllegalArgumentException("baseWaitingDuration value must be positive")
        val testFuncInvoke = attemptAndBaseDurationToFinalDuration.invoke(1, Duration.ofSeconds(1))
        if (testFuncInvoke.isNegative || testFuncInvoke.isZero)
            throw IllegalArgumentException("attemptAndBaseDurationToFinalDuration result must be only positive")

        this.maxAttempts = maxAttempts
        this.baseWaitingDuration = baseWaitingDuration
        this.attemptAndBaseDurationToFinalDuration = attemptAndBaseDurationToFinalDuration
    }

    fun currentAttempt(): Int = attempt

    fun nextStartTime(from: Instant = Instant.now(clock)): Instant? {
        if (attempt > maxAttempts) {
            return null
        }
        return from.plus(attemptAndBaseDurationToFinalDuration.invoke(attempt++, baseWaitingDuration))
    }

    /**
     * always base duration.
     */
    class Constant(
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
        baseWaitingDuration: Duration = DEFAULT_DURATION
    ) : AfterCrashRestartPolicy(
        maxAttempts = maxAttempts,
        baseWaitingDuration = baseWaitingDuration,
        attemptAndBaseDurationToFinalDuration = { _, duration -> duration }
    )

    /**
     * base duration * attempt.
     */
    class Multiply(
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
        baseWaitingDuration: Duration = DEFAULT_DURATION
    ) : AfterCrashRestartPolicy(
        maxAttempts = maxAttempts,
        baseWaitingDuration = baseWaitingDuration,
        attemptAndBaseDurationToFinalDuration = { attempt, duration -> duration.multipliedBy(attempt.toLong()) }
    )

    /**
     * Each list element repeated.
     */
    class TimelineSeconds(
        timeline: List<Long>,
        repeat: Int
    ) : AfterCrashRestartPolicy(
        maxAttempts = timeline.size * repeat,
        baseWaitingDuration = Duration.ofSeconds(timeline[0]),
        attemptAndBaseDurationToFinalDuration = { attempt, _ ->
            (attempt % timeline.size).let { Duration.ofSeconds(timeline[it]) }
        }
    )

    /**
     * ln(base duration * attempt).
     */
    class Ln(
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
        baseWaitingDuration: Duration = DEFAULT_DURATION
    ) : AfterCrashRestartPolicy(
        maxAttempts = maxAttempts,
        baseWaitingDuration = baseWaitingDuration,
        attemptAndBaseDurationToFinalDuration = { attempt, duration ->
            duration.toMillis().toDouble()
                .times(attempt)
                .let(::ln)
                .toLong()
                .let(Duration::ofMillis)
        }
    )

    /**
     *  exp ^ base duration.
     *  Note: very fast overflow.
     */
    class Exp(
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
        baseWaitingDuration: Duration = DEFAULT_DURATION
    ) : AfterCrashRestartPolicy(
        maxAttempts = maxAttempts,
        baseWaitingDuration = baseWaitingDuration,
        attemptAndBaseDurationToFinalDuration = { attempt, duration ->
            duration.toMillis().toDouble()
                .times(attempt)
                .let(::exp)
                .toLong()
                .let(Duration::ofMillis)
        }
    )

    /**
     * base duration ^ attempt.
     * Note: fast overflow.
     */
    class Pow(
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
        baseWaitingDuration: Duration = DEFAULT_DURATION
    ) : AfterCrashRestartPolicy(
        maxAttempts = maxAttempts,
        baseWaitingDuration = baseWaitingDuration,
        attemptAndBaseDurationToFinalDuration = { attempt, duration ->
            duration.toMillis().toDouble()
                .pow(attempt.toDouble())
                .let(Double::toLong)
                .let(Duration::ofMillis)
        }
    )

    /**
     * (base duration * attempt) ^ 2.
     */
    class Pow2(
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
        baseWaitingDuration: Duration = DEFAULT_DURATION
    ) : AfterCrashRestartPolicy(
        maxAttempts = maxAttempts,
        baseWaitingDuration = baseWaitingDuration,
        attemptAndBaseDurationToFinalDuration = { attempt, duration ->
            duration.toMillis().toDouble()
                .times(attempt)
                .pow(2)
                .toLong()
                .let(Duration::ofMillis)
        }
    )
}
