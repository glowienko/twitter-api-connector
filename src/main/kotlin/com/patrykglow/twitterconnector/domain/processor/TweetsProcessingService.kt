package com.patrykglow.twitterconnector.domain.processor

import com.patrykglow.twitterconnector.domain.TweetTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class TweetsProcessingService(
    private val processors: List<TweetProcessor>,
    private val tracker: TweetTracker
) {

    fun fetchAndProcessTweets(keywords: List<String>) {
        runBlocking(Dispatchers.IO) {
            val tweets = async { tracker.getTweetsBatch(keywords) }

            tweets.await()
                .forEach { tweet ->
                    processors
                        .filter { it.canProcess(tweet) }
                        .forEach { it.process(tweet) }
                }
        }

    }

}