package com.virginactive.shared.data.home

import com.virginactive.shared.data.auth.apiErrorJson
import com.virginactive.shared.data.auth.respondJson
import com.virginactive.shared.data.remote.ApiConfig
import com.virginactive.shared.data.remote.createHttpClient
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import com.virginactive.shared.domain.home.HomeRepository
import com.virginactive.shared.domain.home.ManifestBlock
import com.virginactive.shared.domain.profile.ProfileRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class HomeRepositoryTest {

    private val config = ApiConfig(baseUrl = "http://test.local")

    private fun homeRepository(engine: MockEngine): HomeRepository {
        val client = createHttpClient(engine = engine, config = config, enableLogging = false)
        val api = HomeApi(client)
        return HomeRepositoryImpl(api)
    }

    private fun profileRepository(engine: MockEngine): ProfileRepository {
        val client = createHttpClient(engine = engine, config = config, enableLogging = false)
        val api = com.virginactive.shared.data.profile.ProfileApi(client)
        return com.virginactive.shared.data.profile.ProfileRepositoryImpl(api)
    }

    @Test
    fun mixedManifestDecodesOkWithTolerancePreservedThroughRepository() = runTest {
        val engine = MockEngine { _ -> respondJson(MIXED_MANIFEST) }
        val repo = homeRepository(engine)

        val result = repo.getManifest(CLUB_ID)

        assertIs<AppResult.Ok<*>>(result, "200 mixed manifest -> Ok")
        val manifest = (result as AppResult.Ok).value
        assertEquals(3, manifest.blocks.size, "nothing is dropped")
        assertIs<ManifestBlock.Greeting>(manifest.blocks[0])
        assertEquals(
            2,
            manifest.blocks.count { it is ManifestBlock.Unknown },
            "unknown-discriminator + malformed-known-body both become Unknown through the repository",
        )
    }

    @Test
    fun serverErrorMapsToErrServer() = runTest {
        val engine = MockEngine { _ ->
            respondJson(apiErrorJson("server_error", "ChaosFailure"), HttpStatusCode.InternalServerError)
        }
        val repo = homeRepository(engine)

        val result = repo.getManifest(CLUB_ID)

        assertEquals(AppResult.Err(DomainError.Server), result, "500 -> Err(Server); nothing thrown")
    }

    @Test
    fun unauthorizedMapsToErrUnauthorized() = runTest {
        val engine = MockEngine { _ ->
            respondJson(apiErrorJson("unauthorized", "TokenExpired"), HttpStatusCode.Unauthorized)
        }
        val repo = homeRepository(engine)

        val result = repo.getManifest(CLUB_ID)

        assertEquals(AppResult.Err(DomainError.Unauthorized), result, "401 -> Err(Unauthorized)")
    }


    @Test
    fun profile200ReturnsOkWithHomeClubId() = runTest {
        val engine = MockEngine { _ -> respondJson(meResponseJson(homeClubId = CLUB_ID)) }
        val repo = profileRepository(engine)

        val result = repo.getProfile()

        assertIs<AppResult.Ok<*>>(result, "200 /me -> Ok(UserProfile)")
        assertEquals(
            CLUB_ID,
            (result as AppResult.Ok).value.homeClub.id,
            "homeClub.id resolves the clubId (HOME-01)",
        )
    }

    @Test
    fun profileServerErrorMapsToErrServerSoCallerCanFallBack() = runTest {
        val engine = MockEngine { _ ->
            respondJson(apiErrorJson("server_error", "ChaosFailure"), HttpStatusCode.InternalServerError)
        }
        val repo = profileRepository(engine)

        val result = repo.getProfile()

        assertEquals(AppResult.Err(DomainError.Server), result, "500 /me -> Err(Server), nothing thrown")
    }

    private companion object {
        const val CLUB_ID = "club_sea_point"

        fun meResponseJson(
            userId: String = "u-runner",
            firstName: String = "Avid",
            lastName: String = "Runner",
            email: String = "avid.runner@virginactive.mock",
            membershipTier: String = "premium",
            homeClubId: String = CLUB_ID,
            homeClubName: String = "Virgin Active Sea Point",
        ): String =
            """{"id":"$userId","firstName":"$firstName","lastName":"$lastName",""" +
                """"email":"$email","membershipTier":"$membershipTier",""" +
                """"homeClub":{"id":"$homeClubId","name":"$homeClubName"}}"""

        val MIXED_MANIFEST = """
        {
          "blocks": [
            { "type": "greeting", "title": "Good afternoon, Avid" },
            { "type": "experimental", "payload": { "kind": "futureFeature", "title": "Coming soon" } },
            { "type": "hero", "title": 42 }
          ]
        }
        """.trimIndent()
    }
}
