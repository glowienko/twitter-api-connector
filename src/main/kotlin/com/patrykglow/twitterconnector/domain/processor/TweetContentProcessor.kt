package com.patrykglow.twitterconnector.domain.processor

import com.patrykglow.twitterconnector.domain.Tweet
import com.patrykglow.twitterconnector.domain.TweetRepository
import com.patrykglow.twitterconnector.domain.alerts.AlertRepository
import com.patrykglow.twitterconnector.domain.alerts.TweetAlert
import com.patrykglow.twitterconnector.domain.alerts.TweetAlertSender
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionManager
import org.springframework.transaction.annotation.Transactional

private const val WW_3_KEYWORD = "WW3"

@Component
class TweetContentProcessor(
    private val alertRepository: AlertRepository,
    private val tweetRepository: TweetRepository
) : TweetProcessor {
    override fun canProcess(tweet: Tweet): Boolean = tweet.text.contains(WW_3_KEYWORD)

    @Transactional
    override fun process(tweet: Tweet) {
        alertRepository.save(TweetAlert(tweet.id, tweet.author.name, tweet.text))
        tweetRepository.save(tweet)
    }
}