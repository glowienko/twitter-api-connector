package com.patrykglow.twitterconnector.adapters.twitter.api

import com.patrykglow.twitterconnector.TweetTestData.createTweet
import com.patrykglow.twitterconnector.TweetTestData.createTweetResponse
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TweetResponseMapper.toTweet
import com.patrykglow.twitterconnector.domain.Author
import com.patrykglow.twitterconnector.domain.Tweet
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class TweetResponseMapperTest {

    @Test
    fun `should map twitter response correctly for basic fields`() {
        val givenTweetResponse = createTweetResponse()
        val expected = createTweet(givenTweetResponse)
        val result = givenTweetResponse.toTweet()

        result.shouldBeEqualToIgnoringFields(expected, Tweet::createdAt, Tweet::author)
        result.author.shouldBeEqualToIgnoringFields(expected.author, Author::createdAt)
    }

    @Test
    fun `should map twitter response createdAt fields correctly`() {
        val givenDate = Instant.now()
        val result = createTweetResponse(givenDate).toTweet()

        result.createdAt shouldBe givenDate.epochSecond
        result.author.createdAt shouldBe givenDate.epochSecond
    }
}