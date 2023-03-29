package com.patrykglow.twitterconnector.domain.alerts

import com.patrykglow.twitterconnector.domain.TweetId

interface TweetAlertSender {
    fun sendAlert(alert: TweetAlert)
}

data class TweetAlert(
    val id: TweetId,
    val subject: String,
    val message: String,
    val sent: Boolean = false,
    val failuresCount: Int = 0
) {
    fun markAsSent() = copy(sent = true)

    fun incrementFailures() = copy(failuresCount = failuresCount + 1)

}
