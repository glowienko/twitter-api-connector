package com.patrykglow.twitterconnector.domain

import com.patrykglow.twitterconnector.TweetTestData.createTweet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.time.Duration

class FakeFailingTweeterFinder(private val emittingDelay: Duration, private val tweetEmitMaxLimit: Int) : TweetFinder {
    override fun findTweetsBy(keywords: List<String>): Flow<Tweet> =
        flow {
            for (i in 1..tweetEmitMaxLimit) {
                delay(emittingDelay.inWholeMilliseconds)
                if (i == 2) throw IllegalStateException("Fake exception")
                emit(createTweet().also { println("emitting...$i th element") })
            }
        }.flowOn(Dispatchers.IO)
}