package com.spliteasy.spliteasy.util

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    private fun decodePart(part: String): String {
        return String(Base64.decode(part, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
    }

    /** Devuelve el payload del JWT como JSONObject o null si falla */
    fun payload(token: String?): JSONObject? {
        return try {
            if (token.isNullOrBlank()) return null
            val pieces = token.split(".")
            if (pieces.size < 2) return null
            JSONObject(decodePart(pieces[1]))
        } catch (_: Throwable) {
            null
        }
    }

    /** Intenta extraer un id numérico (id / userId / sub) */
    fun userId(token: String?): Long? {
        return try {
            val p = payload(token) ?: return null
            when {
                p.has("id")     -> p.optLong("id")
                p.has("userId") -> p.optLong("userId")
                p.has("sub")    -> p.optString("sub").toLongOrNull()
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    /** Intenta extraer un nombre de usuario razonable (username o email->antes de @) */
    fun username(token: String?): String? {
        val p = payload(token) ?: return null
        val byUsername = p.optString("username", "").ifBlank { null }
        if (byUsername != null) return byUsername
        val email = p.optString("email", "").ifBlank { null }
        return email?.substringBefore("@")
    }
}
