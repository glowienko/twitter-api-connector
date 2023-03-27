package com.patrykglow.twitterconnector.adapters.twitter.api

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.core.Options.DYNAMIC_PORT
import com.patrykglow.twitterconnector.Application
import com.patrykglow.twitterconnector.IntegrationTestConfig
import com.patrykglow.twitterconnector.IntegrationTestFixture.KEYWORDS
import com.patrykglow.twitterconnector.IntegrationTestFixture.create2TweetsStreamWithKeepAliveNewLine
import com.patrykglow.twitterconnector.IntegrationTestFixture.createStreamDisconnectMessage
import com.patrykglow.twitterconnector.IntegrationTestFixture.createStreamWithOneTweetAndLimitMessage
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TWEET_FILTER_STREAM_PATH
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TWEET_KEYWORD_QUERY_PARAM
import com.patrykglow.twitterconnector.domain.BatchOrTimeoutTweetTracker
import com.patrykglow.twitterconnector.domain.TwitterTrackerSettings
import com.patrykglow.twitterconnector.infrastructure.Profiles.INTEGRATION_TEST
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.METHOD_FAILURE
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE
import org.springframework.test.context.ActiveProfiles
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ActiveProfiles(INTEGRATION_TEST)
@SpringBootTest(webEnvironment = NONE, classes = [Application::class, IntegrationTestConfig::class])
@AutoConfigureWireMock(port = DYNAMIC_PORT)
class BatchOrTimeoutTweetTrackerTest {
    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var properties: TwitterTrackerSettings

    @Autowired
    lateinit var batchOrTimeoutTweetTracker: BatchOrTimeoutTweetTracker

    @BeforeEach
    fun setup() {
        wireMockServer.resetAll()
    }

    @Test
    fun `should consume 2 tweets with keep alive new lines in between and return them before timeout`() {
        runBlocking {
            mockGetTweetStreamRequestWithSuccess(create2TweetsStreamWithKeepAliveNewLine())

            val result = async { batchOrTimeoutTweetTracker.trackTweetsOn(KEYWORDS) }

            result.await().size shouldBe 2
        }
    }

    @Test
    fun `should return 0 tweets before timeout when only disconnect message present on a stream`() {
        runBlocking {
            mockGetTweetStreamRequestWithSuccess(createStreamDisconnectMessage())

            val result = async { batchOrTimeoutTweetTracker.trackTweetsOn(KEYWORDS) }

            result.await().size shouldBe 0
        }
    }

    @Test
    fun `should return 1 tweet when tweet and limit notices message present on a stream`() {
        runBlocking {
            mockGetTweetStreamRequestWithSuccess(createStreamWithOneTweetAndLimitMessage())
            val result = async { batchOrTimeoutTweetTracker.trackTweetsOn(KEYWORDS) }

            result.await().size shouldBe 1
        }
    }

    @Test
    fun `should timeout with 0 tweets when response from a stream is longer than maxTracking time`() {
        runBlocking {
            mockGetTweetStreamRequestWithSuccessAfterDelay(
                createStreamWithOneTweetAndLimitMessage(), properties.batchMaxDuration * 2
            )
            val result = async { batchOrTimeoutTweetTracker.trackTweetsOn(KEYWORDS) }

            result.await().size shouldBe 0
        }
    }

    @Test
    fun `should retry after 5xx response from twitter API and then return 2 tweets sucessfully without timeout`() {
        runBlocking {
            mockGetTweetStreamWithFirstErrorThenSuccess(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                create2TweetsStreamWithKeepAliveNewLine()
            )

            val result = async { batchOrTimeoutTweetTracker.trackTweetsOn(KEYWORDS) }

            result.await().size shouldBe 2
        }
    }

    @Test
    fun `should retry after 420 response from twitter API and then return 2 tweets sucessfully without timeout`() {
        runBlocking {
            mockGetTweetStreamWithFirstErrorThenSuccess(METHOD_FAILURE.value(), create2TweetsStreamWithKeepAliveNewLine())

            val result = async { batchOrTimeoutTweetTracker.trackTweetsOn(KEYWORDS) }

            result.await().size shouldBe 2
        }
    }

    @Test
    fun `should not retry on 403 response from twitter API and return empty result`() {
        runBlocking {
            mockGetTweetStreamWithFirstErrorThenSuccess(FORBIDDEN.value(), create2TweetsStreamWithKeepAliveNewLine())

            val result = async { batchOrTimeoutTweetTracker.trackTweetsOn(KEYWORDS) }

            result.await().size shouldBe 0
        }
    }

    private fun mockGetTweetStreamWithFirstErrorThenSuccess(
        errorCode: Int,
        response: String,
        trackKeywords: List<String> = KEYWORDS
    ) {
        wireMockServer.stubFor(
            WireMock.get("/$TWEET_FILTER_STREAM_PATH?$TWEET_KEYWORD_QUERY_PARAM=${trackKeywords.joinToString(",")}")
                .inScenario("First call error then 200 OK")
                .willReturn(aResponse().withStatus(errorCode))
                .willSetStateTo("now give 200 OK")
        )

        wireMockServer.stubFor(
            WireMock.get("/$TWEET_FILTER_STREAM_PATH?$TWEET_KEYWORD_QUERY_PARAM=${trackKeywords.joinToString(",")}")
                .inScenario("First call error then 200 OK")
                .whenScenarioStateIs("now give 200 OK")
                .willReturn(
                    aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_STREAM_JSON_VALUE)
                        .withBody(response)
                )
        );
    }

    private fun mockGetTweetStreamRequestWithSuccess(response: String, trackKeywords: List<String> = KEYWORDS) =
        wireMockServer.stubFor(
            WireMock.get("/$TWEET_FILTER_STREAM_PATH?$TWEET_KEYWORD_QUERY_PARAM=${trackKeywords.joinToString(",")}")
                .willReturn(
                    aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_STREAM_JSON_VALUE)
                        .withBody(response)
                )
        )

    private fun mockGetTweetStreamRequestWithSuccessAfterDelay(
        response: String,
        delay: Duration = 100.milliseconds,
        trackKeywords: List<String> = KEYWORDS
    ) =
        wireMockServer.stubFor(
            WireMock.get("/$TWEET_FILTER_STREAM_PATH?$TWEET_KEYWORD_QUERY_PARAM=${trackKeywords.joinToString(",")}")
                .willReturn(
                    aResponse()
                        .withStatus(OK.value())
                        .withFixedDelay(delay.inWholeMilliseconds.toInt())
                        .withHeader(CONTENT_TYPE, APPLICATION_STREAM_JSON_VALUE)
                        .withBody(response)
                )
        )

}