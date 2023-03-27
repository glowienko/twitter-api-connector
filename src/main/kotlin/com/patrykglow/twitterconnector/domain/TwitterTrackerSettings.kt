package com.patrykglow.twitterconnector.domain

import kotlin.time.Duration

interface TwitterTrackerSettings {
    val batchMaxDuration: Duration
    val batchSize: Int
}