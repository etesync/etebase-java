package com.etesync.journalmanager.util

import okhttp3.Interceptor
import okhttp3.Response

class TokenAuthenticator constructor(internal val host: String?, internal val token: String?) : Interceptor {
    protected val HEADER_AUTHORIZATION = "Authorization"

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
}
