package com.patrykglow.twitterconnector.infrastructure

import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TweeterApiTweetFinder
import com.patrykglow.twitterconnector.adapters.twitter.tracker.TwitterTrackerProperties
import com.patrykglow.twitterconnector.domain.BatchOrTimeoutTweetTracker
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TwitterConnectorConfig {

    @Bean
    fun batchOrTimeoutTweetTracker(
        twitterTrackerProperties: TwitterTrackerProperties,
        tweeterApiTweetFinder: TweeterApiTweetFinder
    ) = BatchOrTimeoutTweetTracker(twitterTrackerProperties, tweeterApiTweetFinder)
}