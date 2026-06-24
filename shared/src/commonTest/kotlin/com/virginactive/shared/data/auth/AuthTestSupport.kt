package com.virginactive.shared.data.auth

import com.virginactive.shared.data.remote.appJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf


fun jsonHeaders() =
    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

fun MockRequestHandleScope.respondJson(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(content = body, status = status, headers = jsonHeaders())

fun refreshResponseJson(
    accessToken: String,
    refreshToken: String,
    tokenType: String = "Bearer",
    expiresIn: Int = 900,
): String =
    """{"accessToken":"$accessToken","refreshToken":"$refreshToken",""" +
        """"tokenType":"$tokenType","expiresIn":$expiresIn}"""

fun loginResponseJson(
    accessToken: String,
    refreshToken: String,
    userId: String,
    firstName: String,
    lastName: String,
    email: String,
    membershipTier: String = "premium",
    homeClubId: String = "club-1",
    homeClubName: String = "Virgin Active Sandton",
): String =
    """{"accessToken":"$accessToken","refreshToken":"$refreshToken",""" +
        """"tokenType":"Bearer","expiresIn":900,""" +
        """"user":{"id":"$userId","firstName":"$firstName","lastName":"$lastName",""" +
        """"email":"$email","membershipTier":"$membershipTier",""" +
        """"homeClub":{"id":"$homeClubId","name":"$homeClubName"}}}"""

fun apiErrorJson(error: String, code: String?, message: String = "error"): String =
    """{"error":"$error","message":"$message",""" +
        (code?.let { """"code":"$it",""" } ?: "") +
        """"requestId":"req-test"}"""

class AuthCallCounters {
    var refreshCalls: Int = 0
    var loginCalls: Int = 0
    var protectedCalls: Int = 0

    val refreshRequestBodies: MutableList<String> = mutableListOf()
}

fun HttpRequestData.bodyText(): String =
    (body as? io.ktor.http.content.TextContent)?.text ?: ""

fun scriptedAuthEngine(
    counters: AuthCallCounters,
    expiredAccessToken: String = "acc1",
    refreshResponse: String = refreshResponseJson("acc2", "ref2"),
    refreshFails: Boolean = false,
    protectedBody: String = """{"ok":true}""",
): MockEngine = MockEngine { request ->
    when {
        request.url.encodedPath == "/auth/refresh" -> {
            counters.refreshCalls++
            counters.refreshRequestBodies += request.bodyText()
            if (refreshFails) {
                respondJson(apiErrorJson("unauthorized", "TokenExpired"), HttpStatusCode.Unauthorized)
            } else {
                respondJson(refreshResponse)
            }
        }

        request.headers[HttpHeaders.Authorization] == "Bearer $expiredAccessToken" -> {
            respondJson(apiErrorJson("unauthorized", "TokenExpired"), HttpStatusCode.Unauthorized)
        }

        else -> {
            counters.protectedCalls++
            respondJson(protectedBody)
        }
    }
}

fun retryScriptEngine(
    sequence: List<HttpStatusCode>,
    retryAfterSeconds: Int? = null,
    body200: String = """{"ok":true}""",
    counter: AuthCallCounters = AuthCallCounters(),
): MockEngine {
    var index = 0
    return MockEngine { _ ->
        counter.protectedCalls++
        val status = sequence.getOrElse(index) { sequence.last() }
        val isFirst = index == 0
        index++
        if (status.value in 200..299) {
            respondJson(body200, status)
        } else {
            val headers = if (retryAfterSeconds != null && isFirst) {
                headersOf(
                    HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
                    HttpHeaders.RetryAfter to listOf(retryAfterSeconds.toString()),
                )
            } else {
                jsonHeaders()
            }
            respond(
                content = apiErrorJson("rate_limited", "RateLimited"),
                status = status,
                headers = headers,
            )
        }
    }
}

val testJson get() = appJson
