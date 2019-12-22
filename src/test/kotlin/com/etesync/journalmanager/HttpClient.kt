package com.etesync.journalmanager

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

    private class TokenAuthenticator internal constructor(internal val host: String?, internal val token: String?) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()

            /* Only add to the host we want. */
            if (host == null || request.url().host() == host) {
                if (token != null && request.header(HEADER_AUTHORIZATION) == null) {
                    request = request.newBuilder()
                            .header(HEADER_AUTHORIZATION, "Token $token")
                            .build()
                }
            }

            return chain.proceed(request)
        }

        companion object {
            protected val HEADER_AUTHORIZATION = "Authorization"
        }
    }
}
