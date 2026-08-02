package dev.elysium.eauth

import java.io.File
import java.security.KeyFactory
import java.security.interfaces.EdECPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

object KeyLoader {
    fun loadPublicKey(file: File): EdECPublicKey {
        val pem = file.readText()

        val bytes = Base64.getDecoder().decode(
            pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")
        )

        return KeyFactory.getInstance("Ed25519")
            .generatePublic(X509EncodedKeySpec(bytes)) as EdECPublicKey
    }
}