package com.patrykglow.twitterconnector.domain.alerts

interface AlertRepository {

    fun save(alert: TweetAlert)
    fun findAllUnsent(): List<TweetAlert>
}