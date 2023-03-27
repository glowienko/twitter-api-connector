package com.patrykglow.twitterconnector.adapters.twitter.api

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "twitter.api")
data class TwitterApiProperties(
    val baseUrl: String,
    val consumerKey: String,
    val consumerSecret: String,
    val accessToken: String,
    val accessTokenSecret: String
)