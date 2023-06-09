package com.patrykglow.twitterconnector.adapters.twitter.api.v1.oauth

import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.WebClientException

class TwitterResponseException(val statusCode: HttpStatusCode) :
    WebClientException("Twitter api error: ${statusCode.value()}")
