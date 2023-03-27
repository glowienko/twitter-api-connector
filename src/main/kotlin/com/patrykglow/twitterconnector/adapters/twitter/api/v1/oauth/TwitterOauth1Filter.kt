package com.patrykglow.twitterconnector.adapters.twitter.api.v1.oauth

import com.patrykglow.twitterconnector.adapters.twitter.api.v1.TwitterApiProperties
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import reactor.core.publisher.Mono

object TwitterOauth1Filter {
    fun twitterOauth1AuthenticationFilter(properties: TwitterApiProperties): (t: ClientRequest) -> Mono<ClientRequest> =
        { clientRequest ->
            Mono.just(
                ClientRequest.from(clientRequest)
                    .header(AUTHORIZATION.lowercase(), buildAuth1Header(properties, clientRequest))
                    .build()
            )
        }

    private fun buildAuth1Header(
        properties: TwitterApiProperties,
        it: ClientRequest
    ) = OAuth10(
        properties.consumerKey,
        properties.consumerSecret,
        properties.accessToken,
        properties.accessTokenSecret
    ).getAuthorizedHeader(
        it.method().name(),
        it.url().toString().substringBefore("?"),
        toQueryParamMap(it.url().query)
    )

    private fun toQueryParamMap(it: String): Map<String, String> {
        val (key, value) = it.split("=")
        return mapOf(key to value)
    }

}