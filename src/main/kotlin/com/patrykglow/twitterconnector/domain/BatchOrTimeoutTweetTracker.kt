package com.patrykglow.twitterconnector.domain

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull

class BatchOrTimeoutTweetTracker(
    private val defaultSettings: TwitterTrackerSettings,
    private val tweetFinder: TweetFinder
) : TweetTracker {
    override suspend fun trackTweetsOn(keywords: List<String>): Map<Author, List<Tweet>> =
        trackTweetsOn(keywords, defaultSettings)

    override suspend fun trackTweetsOn(
        keywords: List<String>,
        trackingSettings: TwitterTrackerSettings
    ): Map<Author, List<Tweet>> {
        val tweets = mutableListOf<Tweet>()

        return withTimeoutOrNull(trackingSettings.batchMaxDuration) {
            tweetFinder.findTweetsBy(keywords)
                .onEach { tweets.add(it) }
                .take(trackingSettings.batchSize)
                .catch { println("No retry error occurred: ${it.message}") }
                .toList()
                .groupByAuthorSortAllAscending()
                .also { println("Batch size reached, returning ${tweets.size} tweets") }
        } ?: tweets.groupByAuthorSortAllAscending().also { println("Timeout reached, returning ${tweets.size} tweets") }
    }

    override suspend fun getTweetsBatch(keywords: List<String>): List<Tweet> {
        return withTimeoutOrNull(defaultSettings.batchMaxDuration) {
            tweetFinder.findTweetsBy(keywords)
                .take(defaultSettings.batchSize)
                .catch { println("No retry error occurred: ${it.message}") }
                .toList()
                .also { println("Batch size reached, returning ${defaultSettings.batchSize} tweets") }
        } ?: emptyList()
    }
}