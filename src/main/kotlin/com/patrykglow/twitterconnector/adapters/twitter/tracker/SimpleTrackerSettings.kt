package com.patrykglow.twitterconnector.adapters.twitter.tracker

import com.patrykglow.twitterconnector.domain.TwitterTrackerSettings
import kotlin.time.Duration

data class SimpleTrackerSettings(
    override val batchMaxDuration: Duration,
    override val batchSize: Int
) : TwitterTrackerSettings