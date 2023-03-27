package com.patrykglow.twitterconnector

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.patrykglow.twitterconnector.infrastructure.Profiles.INTEGRATION_TEST
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!$INTEGRATION_TEST")
class TweetTrackerCommandLineRunner(
    private val twitterSearchService: TweetSearchService,
) : CommandLineRunner {
    override fun run(vararg args: String) {
        runBlocking {
            println()
            twitterSearchService
                .findTweets(listOf("elon"))
                .forEach { println(jacksonObjectMapper().writeValueAsString(it)) }
            println()
            println("FETCHING DONE")
        }
    }
}