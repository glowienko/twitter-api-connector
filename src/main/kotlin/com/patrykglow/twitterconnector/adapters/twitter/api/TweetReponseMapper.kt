package com.patrykglow.twitterconnector.adapters.twitter.api

import com.patrykglow.twitterconnector.domain.Author
import com.patrykglow.twitterconnector.domain.AuthorId
import com.patrykglow.twitterconnector.domain.Tweet
import com.patrykglow.twitterconnector.domain.TweetId
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Locale.ENGLISH

const val TWITTER_DATE_PATTERN = "EEE MMM dd HH:mm:ss Z yyyy"

object TweetResponseMapper {
    private val formatter = ofPattern(TWITTER_DATE_PATTERN, ENGLISH).withZone(ZoneId.of("UTC"))
    fun TweetResponse.toTweet(): Tweet =
        Tweet(
            id = TweetId(raw = id),
            createdAt = parseStringDateToEpochSeconds(createdAt),
            text = text,
            author = Author(
                AuthorId(raw = user.id),
                parseStringDateToEpochSeconds(createdAt = user.createdAt),
                name = user.name,
                screenName = user.screenName
            )
        )

    private fun parseStringDateToEpochSeconds(createdAt: String): Long =
        Instant.from(formatter.parse(createdAt)).epochSecond

}