package com.spliteasy.spliteasy.core

import android.app.Application
import android.content.Context
import com.google.android.recaptcha.Recaptcha
import com.google.android.recaptcha.RecaptchaAction
import com.google.android.recaptcha.RecaptchaClient

object RecaptchaHelper {

    @Volatile private var client: RecaptchaClient? = null

    private suspend fun getClient(appContext: Context, siteKey: String): RecaptchaClient {
        client?.let { return it }
        val application = appContext.applicationContext as Application
        val created = Recaptcha.fetchClient(application, siteKey)
        client = created
        return created
    }

    suspend fun getToken(
        context: Context,
        action: RecaptchaAction = RecaptchaAction.SIGNUP,
        siteKey: String = RecaptchaKeys.ANDROID_SITE_KEY
    ): String {
        val c = getClient(context, siteKey)
        return c.execute(action).getOrThrow()
    }
}
