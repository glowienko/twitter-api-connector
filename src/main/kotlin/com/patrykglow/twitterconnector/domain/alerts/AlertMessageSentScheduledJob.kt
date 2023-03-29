package com.patrykglow.twitterconnector.domain.alerts

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional


open class AlertMessageSentScheduledJob(
    private val repository: AlertRepository,
    private val alertSender: TweetAlertSender
) {
    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedRate = 6000)
    fun run() {
        repository.findAllUnsent().forEach {
            try {
                sendTweetAlert(it)
            } catch (e: Exception) {
                logger.error(e) { "Failed to send tweet alert: $it" }
                repository.save(it.incrementFailures())
            }

        }
    }

    @Transactional
    open fun sendTweetAlert(it: TweetAlert) {
        alertSender.sendAlert(it)
        repository.save(it.markAsSent())
    }
}