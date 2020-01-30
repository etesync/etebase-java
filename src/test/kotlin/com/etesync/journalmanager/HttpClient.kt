package com.etesync.journalmanager

import com.etesync.journalmanager.util.TokenAuthenticator
import okhttp3.*
import java.util.concurrent.TimeUnit

object HttpClient {
    val okHttpClient = OkHttpClient.Builder()
        // set timeouts
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)

        // don't allow redirects by default, because it would break PROPFIND handling
        .followRedirects(false)

        .build()

    public fun withAuthentication(host: String?, token: String): OkHttpClient {
        val authHandler = TokenAuthenticator(host, token)

        return okHttpClient.newBuilder().addNetworkInterceptor(authHandler).build()
    }
}
