package com.patrykglow.twitterconnector.adapters.twitter.api.v1

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "twitter.api.v1")
data class TwitterApiProperties(
    val baseUrl: String,
    val consumerKey: String,
    val consumerSecret: String,
    val accessToken: String,
    val accessTokenSecret: String
)