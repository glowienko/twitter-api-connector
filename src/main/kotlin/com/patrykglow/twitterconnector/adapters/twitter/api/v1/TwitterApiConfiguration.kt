package com.patrykglow.twitterconnector.adapters.twitter.api.v1

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.oauth.TwitterOauth1Filter.twitterOauth1AuthenticationFilter
import com.patrykglow.twitterconnector.adapters.twitter.tracker.TwitterTrackerProperties
import io.github.resilience4j.core.IntervalBiFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

private const val TWITTER_RETRY_NAME = "twitterRetry"
private const val TWITTER_RETRY_MAX_ATTEMPTS = 100
private const val USER_AGENT = "Pgw TwitterConnector"

private val TWITTER_MAX_RETRY_DURATION = 5.minutes.toJavaDuration()

@Configuration
@EnableConfigurationProperties(TwitterApiProperties::class, TwitterTrackerProperties::class)
class TwitterApiConfiguration {

    @Bean
    fun twitterObjectMapper(): ObjectMapper {
        return jacksonMapperBuilder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build()
    }

    @Bean
    fun twitterWebClient(twitterObjectMapper: ObjectMapper, properties: TwitterApiProperties): WebClient =
        WebClient.builder()
            .baseUrl(properties.baseUrl)
            .codecs { it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(twitterObjectMapper)) }
            .filter(ExchangeFilterFunction.ofRequestProcessor(twitterOauth1AuthenticationFilter(properties)))
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .build()

    @Bean
    fun twitterRetry(): Retry {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(TWITTER_RETRY_MAX_ATTEMPTS)
            .waitDuration(TWITTER_MAX_RETRY_DURATION)
            .failAfterMaxAttempts(true)
            .intervalBiFunction(CustomTwitterApiV1RetryIntervalBiFunction() as IntervalBiFunction<Any>)
            .retryOnException(Throwable::checkIfEligibleForRetry)
            .writableStackTraceEnabled(true)
            .build()

        val registry = RetryRegistry.of(config)
        return registry.retry(TWITTER_RETRY_NAME)
    }
}