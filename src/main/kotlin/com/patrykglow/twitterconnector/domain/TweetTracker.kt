package com.patrykglow.twitterconnector.domain

interface TweetTracker {
    suspend fun trackTweetsOn(keywords: List<String>): Map<Author, List<Tweet>>
    suspend fun getTweetsBatch(keywords: List<String>): List<Tweet>
    suspend fun trackTweetsOn(keywords: List<String>, trackingSettings: TwitterTrackerSettings): Map<Author, List<Tweet>>
}