package com.github.pushpavel.cph.services

import com.github.pushpavel.cph.Constants
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.delay
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.DurationUnit
import kotlin.time.toDuration

val LOG = logger<CphBackend>()

@Service
class CphBackend {

    private fun isApiReachable(url: String): Boolean {
        var urlConnection: HttpURLConnection? = null
        try {
            LOG.info("Checking if CPH Backend is reachable at $url")
            urlConnection = URL("$url/ping").openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 5000 // Set timeout in milliseconds
            urlConnection.connect()
            val responseCode = urlConnection.responseCode
            LOG.info("CPH Backend response code $responseCode")
            return responseCode in 200..299 // Check for successful response (2xx)
        } catch (e: Exception) {
            // Handle exceptions like connection timeout or unreachable host
            LOG.info("Exception when trying to check if CPH Backend is reachable $e")
            return false
        } finally {
            urlConnection?.disconnect() // Close the connection
        }
    }

    suspend fun getURL(): String {
        if (!Constants.IS_DEV) throw NotImplementedError("Download cph binary and run them")
        val backendAddress = Constants.DEV_BACKEND_ADDRESS
        val startTime = System.currentTimeMillis()
        while (true) {
            if (isApiReachable(backendAddress)) return backendAddress
            val elapsedTime = (System.currentTimeMillis() - startTime).toDuration(DurationUnit.MILLISECONDS)
            if (elapsedTime > Constants.BACKEND_START_TIMEOUT) {
                throw Exception("Timeout: $elapsedTime >= ${Constants.BACKEND_START_TIMEOUT}, cannot reach $backendAddress")
            }
            delay(500)
        }
    }
}
