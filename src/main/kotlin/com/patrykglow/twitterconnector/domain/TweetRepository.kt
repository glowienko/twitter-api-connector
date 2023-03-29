package com.patrykglow.twitterconnector.domain

interface TweetRepository {
    fun save(tweet: Tweet)
}