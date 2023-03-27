package com.patrykglow.twitterconnector.domain

import kotlin.math.absoluteValue
import kotlin.random.Random

data class Tweet(val id: TweetId, val createdAt: Long, val text: String, val author: Author)
data class Author(val id: AuthorId, val createdAt: Long, val name: String, val screenName: String)


@JvmInline
value class TweetId(val raw: Long) {
    companion object {
        @JvmStatic
        fun generateRandom() = TweetId(raw = Random.nextLong().absoluteValue)
    }
}

@JvmInline
value class AuthorId(val raw: Long) {
    companion object {
        @JvmStatic
        fun generateRandom() = AuthorId(raw = Random.nextLong().absoluteValue)
    }
}