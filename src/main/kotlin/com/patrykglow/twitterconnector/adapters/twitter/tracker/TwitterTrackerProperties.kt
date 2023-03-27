package com.patrykglow.twitterconnector.adapters.twitter.tracker

import com.patrykglow.twitterconnector.domain.TwitterTrackerSettings
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration
import kotlin.time.toKotlinDuration


@ConfigurationProperties(prefix = "twitter.tracker")
data class TwitterTrackerProperties(
    val maxDuration: Duration,
    override val batchSize: Int
) : TwitterTrackerSettings {
    override val batchMaxDuration: kotlin.time.Duration
        get() = maxDuration.toKotlinDuration()

}