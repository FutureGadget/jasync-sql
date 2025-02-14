package com.github.jasync.sql.db.mysql.encoder.auth

import java.nio.charset.Charset

object MySQLNativePasswordAuthentication : AuthenticationMethod {

    private val EmptyArray = ByteArray(0)

    override fun generateAuthentication(charset: Charset, password: String?, seed: ByteArray?): ByteArray {
        requireNotNull(seed) { "Seed should not be null" }

        return if (password != null) {
            AuthenticationScrambler.scramble411("SHA-1", password, charset, seed, true)
        } else {
            EmptyArray
        }
    }
}
