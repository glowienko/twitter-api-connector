package com.patrykglow.twitterconnector.adapters.twitter.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

sealed interface TwitterGenericResponse

data class TweetResponse(
    val id: Long,
    val createdAt: String,
    val text: String,
    val user: TwitterUser
)

data class TwitterUser(val id: Long, val createdAt: String, val name: String, val screenName: String)

data class LimitNoticesResponse(val limit: TrackLimitNotice)
data class DisconnectResponse(val disconnect: DisconnectMessage)
data class StallWarningsResponse(val warning: StallWarning)

data class TrackLimitNotice(val track: Long)
data class DisconnectMessage(val code: DisconnectionCode, val streamName: String, val reason: String)
data class StallWarning(val code: String, val message: String, val percentFull: Long)

enum class DisconnectionCode(val code: Long, val codeName: String, val description: String) {
    SHUTDOWN(1, "Shutdown", "The stream is being disconnected due to a temporary issue with the underlying stream."),
    DUPLICATE_STREAM(
        2,
        "Duplicate Stream",
        "The stream is being disconnected due to the account already being connected elsewhere."
    ),
    STALL(3, "Stall", "The stream is being disconnected due to the stall_timeout being reached."),
    NORMAL(4, "Normal", "The stream is being disconnected at the request of the site."),
    ADMIN_LOGOUT(5, "Admin Logout", "The stream is being disconnected due to the account logging in elsewhere."),
    MAX_MESSAGE_LIMIT(
        6,
        "Max Message Limit",
        "The stream is being disconnected due to the account reaching its allocated message limit."
    ),
    STREAM_EXCEPTION(7, "Stream Exception", "The stream is being disconnected due to a network issue."),
    BROKER_STALL(8, "Broker Stall", "The stream is being disconnected due to the broker being unavailable."),
    SHED_LOAD(9, "Shed Load", "The stream is being disconnected due to the site being unable to handle the load.");


    @JsonCreator
    fun fromCode(code: Long): DisconnectionCode =
        values().firstOrNull { it.code == code } ?: throw IllegalArgumentException("Unknown code: $code")

    @JsonValue
    fun toCode(): Long = code
}