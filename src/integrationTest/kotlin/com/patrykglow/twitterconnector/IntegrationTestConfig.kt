package com.patrykglow.twitterconnector

import com.patrykglow.twitterconnector.adapters.twitter.api.checkIfEligibleForRetry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary


@TestConfiguration
class IntegrationTestConfig {

    @Bean
    @Primary
    fun integrationRetry(): Retry {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(10)
            .failAfterMaxAttempts(true)
            .intervalFunction { attempts: Int -> (100 * attempts.toLong()).also { println("retry, backoff millis: $it") } }
            .retryOnException(Throwable::checkIfEligibleForRetry)
            .writableStackTraceEnabled(true)
            .build()

        val registry = RetryRegistry.of(config)
        return registry.retry("integrationRetry")
    }
}