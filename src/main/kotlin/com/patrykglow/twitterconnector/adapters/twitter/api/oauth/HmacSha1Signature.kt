package com.patrykglow.twitterconnector.adapters.twitter.api.oauth

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SignatureException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal object HmacSha1Signature {
    private const val HMAC_SHA1_ALGORITHM = "HmacSHA1"
    @JvmStatic
    @Throws(SignatureException::class, NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun calculateRFC2104HMAC(data: String, key: String): ByteArray {
        val signingKey = SecretKeySpec(key.toByteArray(), HMAC_SHA1_ALGORITHM)
        val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
        mac.init(signingKey)
        return mac.doFinal(data.toByteArray())
    }
}