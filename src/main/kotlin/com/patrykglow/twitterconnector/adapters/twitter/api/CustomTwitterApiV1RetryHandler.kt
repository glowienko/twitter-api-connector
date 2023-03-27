package com.patrykglow.twitterconnector.adapters.twitter.api

import com.patrykglow.twitterconnector.adapters.twitter.api.oauth.TwitterResponseException
import io.github.resilience4j.core.IntervalBiFunction
import io.github.resilience4j.core.functions.Either
import org.springframework.http.HttpStatus.METHOD_FAILURE
import kotlin.math.pow

private val retriableTwitterClientErrorCodes = listOf(METHOD_FAILURE.value())

class CustomTwitterApiV1RetryIntervalBiFunction : IntervalBiFunction<Any> {
    /**
     * @return the interval in milliseconds
     */
    override fun apply(attempt: Int, result: Either<Throwable, Any>): Long {
        return when {
            result.isLeft -> {
                calculateIntervalForClientResponse(result.left, attempt)
            }
            //Back off linearly for TCP/IP level network errors. These problems are generally temporary and tend to clear quickly. Increase the delay in reconnects by 250ms each attempt, up to 16 seconds.
            else -> (250 * attempt.toLong()).also { println("retry: not throwable, attempt $attempt, backoff: $it") }
        }
    }

    private fun calculateIntervalForClientResponse(
        response: Throwable,
        attempt: Int
    ): Long {
        when (response) {
            is TwitterResponseException -> {
                //Back off exponentially for HTTP 420 errors. Start with a 1 minute wait and double each attempt.
                if (response.statusCode == METHOD_FAILURE) {
                    return (2.toDouble().pow(attempt - 1)
                        .toLong() * 1000 * 60).also { println("retry: 420 status, attempt $attempt, backoffExp: $it") }
                }
                // HTTP errors for which reconnecting would be appropriate. Start with a 5 second wait, doubling each attempt, up to 320 seconds.
                return if (response.statusCode.is5xxServerError) return (5000 * attempt.toDouble().pow(2)
                    .toLong()).also { println("retry: 5xx status, attempt $attempt, backoffExp: $it") }
                else (250 * attempt.toLong()).also { println("retry: 4xx status, attempt $attempt, backoffExp: $it") }
            }

            else -> return (250 * attempt.toLong()).also { println("retry: other error, attempt $attempt, backoffExp: $it") }
        }
    }
}

fun Throwable.checkIfEligibleForRetry(): Boolean =
    (this is TwitterResponseException && this.statusCode.is5xxServerError
            || this is TwitterResponseException && retriableTwitterClientErrorCodes.contains(this.statusCode.value()))
        .also { println("Retry evaluation: $it, error: ${this.message}") }
