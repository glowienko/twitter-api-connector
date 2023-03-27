package com.patrykglow.twitterconnector.domain

import com.patrykglow.twitterconnector.adapters.twitter.tracker.SimpleTrackerSettings
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class BatchOrTimeoutTweetTrackerTest {

    private val batchOrTimeoutTweetTracker = BatchOrTimeoutTweetTracker(
        SimpleTrackerSettings(batchMaxDuration = 1.seconds, batchSize = 2),
        FakeSuccessfulTweeterFinder(emittingDelay = 100.milliseconds, tweetEmitMaxLimit = 10)
    )

    private val throwingTweetTracker = BatchOrTimeoutTweetTracker(
        SimpleTrackerSettings(batchMaxDuration = 1.seconds, batchSize = 2),
        FakeFailingTweeterFinder(emittingDelay = 100.milliseconds, tweetEmitMaxLimit = 10)
    )

    @Test
    fun `should return all 2 elements limiting to batch size before timeout`() {
        runBlocking {
            val result = async {
                batchOrTimeoutTweetTracker.trackTweetsOn(listOf("keyword"), SimpleTrackerSettings(350.seconds, 2))
            }

            result.await().size shouldBe 2
        }
    }

    @Test
    fun `should return first 3 elements and then timeout on maxBatchDuration`() {
        runBlocking {
            val result = async {
                batchOrTimeoutTweetTracker.trackTweetsOn(listOf("keyword"), SimpleTrackerSettings(350.milliseconds, 55))
            }

            result.await().size shouldBe 3
        }
    }

    @Test
    fun `should handle exception during tweet finding and return only 1 tweet emitted by the finder`() {
        runBlocking {
            val result = async {
                throwingTweetTracker.trackTweetsOn(listOf("keyword"), SimpleTrackerSettings(500.seconds, 3))
            }

            result.await().size shouldBe 1
        }
    }
}