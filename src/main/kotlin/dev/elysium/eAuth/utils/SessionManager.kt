package dev.elysium.eAuth.utils

import java.time.Instant

object SessionManager {
    private val sessions = mutableMapOf<String, Long>()

    fun isValid(ip: String, nick: String): Boolean {
        val key = "$ip:$nick"
        val expiry = sessions[key] ?: return false
        return Instant.now().epochSecond < expiry
    }

    fun addSession(ip: String, nick: String, durationSeconds: Long = 3600) {
        val key = "$ip:$nick"
        val expiry = Instant.now().epochSecond + durationSeconds
        sessions[key] = expiry
    }

    fun removeSession(ip: String, nick: String) {
        sessions.remove("$ip:$nick")
    }

    fun clearAll() {
        sessions.clear()
    }
}