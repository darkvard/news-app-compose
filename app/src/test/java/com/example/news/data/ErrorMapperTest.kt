package com.example.news.data

import com.example.news.data.remote.isEndOfPagination
import com.example.news.data.remote.toDataError
import com.example.news.domain.model.DataError
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun `offline exceptions map to NoConnection`() {
        assertTrue(UnknownHostException().toDataError() is DataError.NoConnection)
        assertTrue(ConnectException().toDataError() is DataError.NoConnection)
    }

    @Test
    fun `socket timeout maps to Timeout`() {
        assertTrue(SocketTimeoutException().toDataError() is DataError.Timeout)
    }

    @Test
    fun `quota responses map to RateLimited`() {
        assertTrue(httpError(426).toDataError() is DataError.RateLimited)
        assertTrue(httpError(429).toDataError() is DataError.RateLimited)
    }

    @Test
    fun `other http errors keep their code`() {
        val error = httpError(500).toDataError()
        assertTrue(error is DataError.Server)
        assertEquals(500, (error as DataError.Server).code)
    }

    @Test
    fun `unexpected exceptions map to Unknown`() {
        assertTrue(IllegalStateException().toDataError() is DataError.Unknown)
    }

    @Test
    fun `pagination ends at the api result cap`() {
        // 100 results available but the free plan caps at 100 -> page 5 of 20 is the last one
        assertFalse(isEndOfPagination(page = 4, pageSize = 20, receivedCount = 20, totalResults = 500, maxResults = 100))
        assertTrue(isEndOfPagination(page = 5, pageSize = 20, receivedCount = 20, totalResults = 500, maxResults = 100))
    }

    @Test
    fun `pagination ends when fewer results than the cap exist`() {
        assertTrue(isEndOfPagination(page = 2, pageSize = 20, receivedCount = 15, totalResults = 35, maxResults = 100))
    }

    @Test
    fun `pagination ends on an empty page`() {
        assertTrue(isEndOfPagination(page = 1, pageSize = 20, receivedCount = 0, totalResults = 0, maxResults = 100))
    }
}
