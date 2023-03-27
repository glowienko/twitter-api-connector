package com.patrykglow.twitterconnector

import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TWITTER_DATE_PATTERN
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TweetResponse
import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TwitterUser
import com.patrykglow.twitterconnector.domain.Author
import com.patrykglow.twitterconnector.domain.AuthorId
import com.patrykglow.twitterconnector.domain.Tweet
import com.patrykglow.twitterconnector.domain.TweetId
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Locale.ENGLISH

object TweetTestData {
    const val RAW_TWEETER_CREATED_AT = "Wed Oct 10 20:19:24 +0000 2018"
    val twitterDateFormatter = ofPattern(TWITTER_DATE_PATTERN, ENGLISH).withZone(ZoneId.of("UTC"))

    val tweet = Tweet(
        id = TweetId.generateRandom(),
        createdAt = Instant.now().toEpochMilli(),
        text = "tweet text",
        author = Author(
            id = AuthorId.generateRandom(),
            createdAt = Instant.now().toEpochMilli(),
            name = "author name",
            screenName = "author screen name"
        )
    )

    val tweet1 = createTweet(authorId = 1, createdAt = 1)
    val tweet2 = createTweet(authorId = 2, createdAt = 1)


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

    fun createTweet(tweetResponse: TweetResponse, createdAt: Instant = Instant.now()): Tweet =
        Tweet(
            id = TweetId(tweetResponse.id),
            createdAt = createdAt.toEpochMilli(),
            text = tweetResponse.text,
            author = Author(
                id = AuthorId(tweetResponse.user.id),
                createdAt = createdAt.toEpochMilli(),
                name = tweetResponse.user.name,
                screenName = tweetResponse.user.screenName
            )
        )

    fun createTweetWith(
        author: Author,
        id: Long = TweetId.generateRandom().raw,
        createdAt: Long = Instant.now().toEpochMilli(),
        text: String = "tweet text",
    ) = Tweet(
        id = TweetId(id),
        createdAt = createdAt,
        text = text,
        author = author
    )

    fun createAuthor(
        id: Long = AuthorId.generateRandom().raw,
        createdAt: Long = Instant.now().toEpochMilli(),
        name: String = "author name",
        screenName: String = "author screen name"
    ) = Author(
        id = AuthorId(id),
        createdAt = createdAt,
        name = name,
        screenName = screenName
    )


    fun createTweet(
        id: Long = TweetId.generateRandom().raw,
        authorId: Long = AuthorId.generateRandom().raw,
        createdAt: Long = Instant.now().toEpochMilli(),
        text: String = "tweet text",
    ) = Tweet(
        id = TweetId(id),
        createdAt = createdAt,
        text = "$text $id",
        author = Author(
            id = AuthorId(authorId),
            createdAt = createdAt,
            name = "author name $authorId",
            screenName = "author screen name $authorId"
        )
    )

    fun createTweetsWithSameAuthor(
        authorId: Long = AuthorId.generateRandom().raw,
        createdAt: Long = Instant.now().toEpochMilli(),
        text: String = "tweet text",
        size: Int = 10
    ) = List(size) {
        Tweet(
            id = TweetId.generateRandom(),
            createdAt = createdAt,
            text = text,
            author = Author(
                id = AuthorId(authorId),
                createdAt = createdAt,
                name = "author name",
                screenName = "author screen name"
            )
        )
    }
}