package com.patrykglow.twitterconnector.domain.processor

import com.patrykglow.twitterconnector.domain.Tweet
import com.patrykglow.twitterconnector.domain.alerts.AlertRepository
import com.patrykglow.twitterconnector.domain.alerts.TweetAlert
import com.patrykglow.twitterconnector.domain.alerts.TweetAlertSender
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private const val ELON_MUSK = "Elon Musk"

@Component
class ElonTweetsProcessors(private val alertRepository: AlertRepository) : TweetProcessor {
    override fun canProcess(tweet: Tweet): Boolean = tweet.author.name == ELON_MUSK

    @Transactional
    override fun process(tweet: Tweet) {
        alertRepository.save(TweetAlert(tweet.id, tweet.author.name, tweet.text))
    }
}