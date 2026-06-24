package com.virginactive.shared.data.booking

import com.virginactive.shared.data.auth.respondJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode


fun bookingResponseJson(
    bookingId: String = "5cb53d29-0000-0000-0000-000000000000",
    status: String = "booked",
    waitlistPosition: Int? = null,
    classId: String = "sp-lunchtime-spin::2026-06-24",
    clubId: String = "club_sea_point",
    title: String = "Lunchtime Spin",
    trainer: String = "Thabo M.",
    type: String = "spin",
    startsAt: String = "2026-06-24T12:00:00+02:00",
    endsAt: String = "2026-06-24T12:45:00+02:00",
    spots: Int = 20,
    available: Int = 5,
    waitlistCount: Int = 0,
    classStatus: String = "available",
    userBookingStatus: String = "booked",
): String =
    """{"bookingId":"$bookingId","status":"$status",""" +
        (waitlistPosition?.let { """"waitlistPosition":$it,""" } ?: "") +
        """"classInstance":{""" +
        """"classId":"$classId","clubId":"$clubId","title":"$title","trainer":"$trainer",""" +
        """"type":"$type","startsAt":"$startsAt","endsAt":"$endsAt",""" +
        """"spots":$spots,"available":$available,"waitlistCount":$waitlistCount,""" +
        """"status":"$classStatus","userBookingStatus":"$userBookingStatus"}}"""

class BookingCallRecorder {
    val encodedPaths: MutableList<String> = mutableListOf()

    val methods: MutableList<HttpMethod> = mutableListOf()

    val attempts: Int get() = encodedPaths.size

    val lastEncodedPath: String? get() = encodedPaths.lastOrNull()
}

fun recordingBookingEngine(
    recorder: BookingCallRecorder,
    status: HttpStatusCode = HttpStatusCode.Created,
    body: String = bookingResponseJson(),
): MockEngine = MockEngine { request ->
    recorder.encodedPaths += request.url.encodedPath
    recorder.methods += request.method
    if (status == HttpStatusCode.NoContent) {
        respondJson("", status)
    } else {
        respondJson(body, status)
    }
}
