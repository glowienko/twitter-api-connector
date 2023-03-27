package com.patrykglow.twitterconnector.adapters.twitter.api.v1.oauth

import com.patrykglow.twitterconnector.adapters.twitter.api.v1.oauth.HmacSha1Signature.calculateRFC2104HMAC
import org.apache.commons.codec.binary.Base64
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.SignatureException
import java.util.*

class OAuth10(
    private val consumer_key: String,
    private val consumer_secret_key: String,
    private val access_token: String,
    private val access_token_sicret: String
) {
    private val secureRandom = SecureRandom()
    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        SignatureException::class,
        InvalidKeyException::class
    )
    fun getAuthorizedHeader(method: String, URI: String, params: Map<String, String>): String {
        val headers: MutableMap<String, String> = LinkedHashMap()
        headers["oauth_consumer_key"] = consumer_key
        headers["oauth_nonce"] = generateNonce()
        headers["oauth_signature"] = ""
        headers["oauth_signature_method"] = "HMAC-SHA1"
        headers["oauth_timestamp"] = generateTimestamp().toString()
        headers["oauth_token"] = access_token
        headers["oauth_version"] = "1.0"
        headers.replace("oauth_signature", getSignature(method, URI, headers, params))
        val outputHeader = StringBuilder("OAuth ")
        for ((key, value) in headers) {
            outputHeader
                .append(percentEncode(key))
                .append("=")
                .append("\"")
                .append(percentEncode(value))
                .append("\"")
                .append(", ")
        }
        outputHeader.deleteCharAt(outputHeader.lastIndexOf(", "))
        return outputHeader.toString()
    }

    private fun generateTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }

    private fun generateNonce(): String {
        val r = ByteArray(32)
        secureRandom.nextBytes(r)
        return Base64
            .encodeBase64String(r)
            .replace("[^A-Za-z0-9]".toRegex(), "")
    }

    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        SignatureException::class,
        InvalidKeyException::class
    )
    private fun getSignature(
        method: String,
        URI: String,
        headers: Map<String, String>,
        params: Map<String, String>
    ): String {
        val transitHeaders: MutableMap<String, String> = LinkedHashMap(headers)
        transitHeaders.remove("oauth_signature")
        val hmacSha1 = calculateRFC2104HMAC(getSignatureBaseString(method, URI, transitHeaders, params), signingKey)
        val oauth10Signature = Base64.encodeBase64String(hmacSha1)
        return oauth10Signature
    }

    @get:Throws(UnsupportedEncodingException::class)
    private val signingKey: String
        get() = percentEncode(consumer_secret_key) +
                "&" +
                percentEncode(access_token_sicret)

    @Throws(UnsupportedEncodingException::class)
    private fun percentEncode(input: String): String {
        return URLEncoder.encode(input, "UTF-8")
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getSignatureBaseString(
        method: String,
        URI: String,
        headers: Map<String, String>,
        params: Map<String, String>
    ): String {
        val sb = StringBuilder()
        sb
            .append(method.uppercase(Locale.getDefault()))
            .append("&")
            .append(percentEncode(URI))
            .append("&")
            .append(percentEncode(getParametrString(headers, params)))
        return sb.toString()
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getParametrString(
        headers: Map<String, String>,
        params: Map<String, String>?
    ): String {
        val out: SortedMap<String, String> = TreeMap()
        if (params != null) {
            out.putAll(percentEncode(params))
        }
        out.putAll(percentEncode(headers))

        val sb = StringBuilder()
        val keys: List<String> = ArrayList(out.keys)
        for (key in keys) {
            sb.append(key)
                .append("=")
                .append(out[key])
                .append("&")
        }
        sb.deleteCharAt(sb.lastIndexOf("&"))
        val parameterString = sb.toString()
        return parameterString
    }

    private fun percentEncode(map: Map<String, String>): Map<String, String> {
        val out: MutableMap<String, String> = HashMap()
        map.forEach { (a: String, b: String) ->
            try {
                out[percentEncode(a)] = percentEncode(b)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        return out
    }
}