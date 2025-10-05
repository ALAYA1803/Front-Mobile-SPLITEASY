package com.spliteasy.spliteasy.core

import android.content.Context
import com.google.android.gms.recaptcha.Recaptcha
import com.google.android.gms.recaptcha.RecaptchaAction
import com.google.android.gms.recaptcha.RecaptchaClient
import com.google.android.gms.recaptcha.RecaptchaHandle
import com.google.android.gms.recaptcha.RecaptchaResultData
import kotlinx.coroutines.tasks.await

object RecaptchaHelper {

    suspend fun getToken(
        context: Context,
        action: RecaptchaAction = RecaptchaAction("SIGNUP"),
        siteKey: String = RecaptchaKeys.ANDROID_SITE_KEY
    ): String {
        val client: RecaptchaClient = Recaptcha.getClient(context)
        val handle: RecaptchaHandle = client.init(siteKey).await()
        val result: RecaptchaResultData = client.execute(handle, action).await()
        return result.tokenResult
    }
}
