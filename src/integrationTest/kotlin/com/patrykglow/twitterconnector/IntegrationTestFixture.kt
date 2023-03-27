package com.patrykglow.twitterconnector

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.DisconnectMessage
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.DisconnectResponse
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.DisconnectionCode
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.LimitNoticesResponse
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TWITTER_DATE_PATTERN
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TrackLimitNotice
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TweetResponse
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TwitterUser
import com.patrykglow.twitterconnector.domain.AuthorId
import com.patrykglow.twitterconnector.domain.TweetId
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*

object IntegrationTestFixture {
    const val KEYWORD = "keyword"
    val KEYWORDS = listOf(KEYWORD)

    const val DISCONNECT_MESSAGE_JSON_STRING = "{\"disconnect\":{\"code\":5,\"stream_name\":\"user\",\"reason\":\"\"}}"

    private val MAPPER = jacksonMapperBuilder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()

    private val disconnectResponse =
        DisconnectResponse(DisconnectMessage(DisconnectionCode.STREAM_EXCEPTION, "tweet stream", ""))
    private val limitResponse = LimitNoticesResponse(TrackLimitNotice(123))

    private val twitterDateFormatter = ofPattern(TWITTER_DATE_PATTERN, Locale.ENGLISH).withZone(ZoneId.of("UTC"))

    fun createTweetResponse(createdAt: Instant = Instant.now()) =
        TweetResponse(
            id = TweetId.generateRandom().raw,
            createdAt = twitterDateFormatter.format(createdAt),
            text = "tweet text",
            user = TwitterUser(
                id = AuthorId.generateRandom().raw,
                createdAt = twitterDateFormatter.format(createdAt),
                name = "author name",
                screenName = "author screen name"
            )
        )


    fun create2TweetsStreamWithKeepAliveNewLine() = """
                                    ${MAPPER.writeValueAsString(createTweetResponse())}
                                    
                                    
                                    ${MAPPER.writeValueAsString(createTweetResponse())}
                                """.trimIndent()

    fun createStreamWithOneTweetAndLimitMessage() = """
                                    ${MAPPER.writeValueAsString(createTweetResponse())}
                                    
                                    ${MAPPER.writeValueAsString(limitResponse)}
                                """.trimIndent()

    fun createStreamDisconnectMessage() = """                                    
                                    ${MAPPER.writeValueAsString(disconnectResponse)}
                                """.trimIndent()
}