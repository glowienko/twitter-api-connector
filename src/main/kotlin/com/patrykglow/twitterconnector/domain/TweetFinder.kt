package com.patrykglow.twitterconnector.domain

import kotlinx.coroutines.flow.Flow

interface TweetFinder {
    fun findTweetsBy(keywords: List<String>): Flow<Tweet>
}