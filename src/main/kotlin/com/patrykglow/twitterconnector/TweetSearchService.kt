package com.patrykglow.twitterconnector

import com.patrykglow.twitterconnector.domain.Author
import com.patrykglow.twitterconnector.domain.Tweet
import com.patrykglow.twitterconnector.domain.TweetTracker
import com.patrykglow.twitterconnector.domain.TwitterTrackerSettings
import org.springframework.stereotype.Component

@Component
class TweetSearchService(private val tweetTracker: TweetTracker) {

    suspend fun findTweets(keywords: List<String>): Map<Author, List<Tweet>> =
        tweetTracker.trackTweetsOn(keywords)

    suspend fun findTweets(keywords: List<String>, settings: TwitterTrackerSettings): Map<Author, List<Tweet>> =
        tweetTracker.trackTweetsOn(keywords, settings)
}