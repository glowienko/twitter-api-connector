package com.patrykglow.twitterconnector.domain.processor

import com.patrykglow.twitterconnector.domain.Tweet

sealed interface TweetProcessor {
    fun canProcess(tweet: Tweet): Boolean
    fun process(tweet: Tweet)
}