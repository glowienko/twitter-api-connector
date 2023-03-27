package com.patrykglow.twitterconnector.adapters.twitter.api.v1

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TweetResponseMapper.toTweet
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.oauth.TwitterResponseException
import com.patrykglow.twitterconnector.domain.Tweet
import com.patrykglow.twitterconnector.domain.TweetFinder
import io.github.resilience4j.kotlin.retry.retry
import io.github.resilience4j.retry.Retry
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import reactor.core.publisher.Mono

const val TWEET_FILTER_STREAM_PATH = "statuses/filter.json"
const val TWEET_KEYWORD_QUERY_PARAM = "track"

@Component
class TweeterApiTweetFinder(
    private val twitterWebClient: WebClient,
    private val retry: Retry,
    private val twitterObjectMapper: ObjectMapper,
    private val metricsRegistry: MeterRegistry
) : TweetFinder {
    override fun findTweetsBy(keywords: List<String>): Flow<Tweet> {
        return getTweetsStream(keywords)
            .onEach { logAndCountMetric(it) }
            .map { it.toTweet() }
    }

    @Counted(value = "twitter.api.v1.tweets.stream.tweets.count")
    @Timed(value = "twitter.api.v1.tweets.stream.time", longTask = true, percentiles = [0.95, 0.99], histogram = true)
    private fun getTweetsStream(keywords: List<String>): Flow<TweetResponse> {
        return twitterWebClient.get()
            .uri { builder ->
                builder
                    .path(TWEET_FILTER_STREAM_PATH)
                    .queryParam(TWEET_KEYWORD_QUERY_PARAM, keywords)
                    .build()
            }
            .accept(MediaType.APPLICATION_STREAM_JSON)
            .retrieve()
            .onStatus(
                { it.isError },
                { Mono.error<TwitterResponseException>(TwitterResponseException(it.statusCode())) }
            )
            .bodyToFlow<JsonNode>()
            .filterNotNull()
            .filter { it.has(TweetResponse::text.name) }
            .map { twitterObjectMapper.treeToValue(it, TweetResponse::class.java) }
            .retry(retry)
            .flowOn(Dispatchers.IO)
    }

    private fun logAndCountMetric(it: TweetResponse) =
        metricsRegistry.counter("twitter.api.v1.tweets.stream.tweets.success.count").increment()
            .also { println("got tweet response: $it") }

}