package com.etesync.journalmanager

import com.etesync.journalmanager.HttpClient
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthenticatorTest {
    private var httpClient: OkHttpClient? = null
    private var remote: HttpUrl? = null

    @Before
    @Throws(IOException::class)
    fun setUp() {
        httpClient = HttpClient.okHttpClient
        remote = HttpUrl.parse("http://localhost:8000") // FIXME: hardcode for now, should make configureable
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
    }

    @Test
    @Throws(IOException::class, Exceptions.HttpException::class)
    fun testAuthToken() {
        val journalAuthenticator = JournalAuthenticator(httpClient!!, remote!!)
        val authToken = journalAuthenticator.getAuthToken(Helpers.USER, Helpers.PASSWORD)
        assertNotEquals(authToken!!.length.toLong(), 0)

        val httpClient2 = HttpClient.withAuthentication(null, authToken)
        val journalAuthenticator2 = JournalAuthenticator(httpClient2, remote!!)
        journalAuthenticator2.invalidateAuthToken(authToken)
    }

    @Test(expected = Exceptions.UnauthorizedException::class)
    @Throws(Exceptions.IntegrityException::class, Exceptions.VersionTooNewException::class, IOException::class, Exceptions.HttpException::class)
    fun testNoUser() {
        val journalAuthenticator = JournalAuthenticator(httpClient!!, remote!!)
        val authToken = journalAuthenticator.getAuthToken(Helpers.USER, "BadPassword")
        assertNotEquals(authToken!!.length.toLong(), 0)
    }
}
