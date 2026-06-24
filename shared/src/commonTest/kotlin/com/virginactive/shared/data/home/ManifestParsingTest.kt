package com.virginactive.shared.data.home

import com.virginactive.shared.domain.home.ImageRef
import com.virginactive.shared.domain.home.ManifestAction
import com.virginactive.shared.domain.home.ManifestBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ManifestParsingTest {

    @Test
    fun knownBlockTypesDecodeToTypedCases() {
        val manifest = decodeManifest(USER1_MANIFEST)

        assertEquals(8, manifest.blocks.size)
        assertIs<ManifestBlock.Greeting>(manifest.blocks[0])
        assertEquals("Good afternoon, Avid", (manifest.blocks[0] as ManifestBlock.Greeting).title)
        assertIs<ManifestBlock.Hero>(manifest.blocks[1])
        assertIs<ManifestBlock.MyClub>(manifest.blocks[2])
        assertIs<ManifestBlock.ClassCarousel>(manifest.blocks[3])
        assertIs<ManifestBlock.MyRewards>(manifest.blocks[4])
        assertIs<ManifestBlock.MyGoals>(manifest.blocks[5])
        assertIs<ManifestBlock.Promotion>(manifest.blocks[6])
        assertEquals(ManifestBlock.Unknown, manifest.blocks[7])
    }

    @Test
    fun knownImageRefsAndNestedActionsDecode() {
        val carousel = decodeManifest(USER1_MANIFEST)
            .blocks
            .filterIsInstance<ManifestBlock.ClassCarousel>()
            .single()

        assertEquals(ManifestAction.OpenTimetable(clubId = "club_sea_point"), carousel.viewAllAction)
        val first = carousel.items.first()
        assertEquals(ImageRef.CLASS_SPIN, first.image)
        assertEquals("2026-06-24T12:00:00+02:00", first.startsAt)
        assertEquals(
            ManifestAction.OpenClass(clubId = "club_sea_point", classId = "sp-lunchtime-spin::2026-06-24"),
            first.actionRef,
        )
    }

    @Test
    fun nullableFieldsTolerateAbsentValues() {
        val manifest = decodeManifest(USER2_MANIFEST)

        assertEquals(7, manifest.blocks.size)
        assertTrue(manifest.blocks.none { it is ManifestBlock.MyRewards })
        val goal = manifest.blocks.filterIsInstance<ManifestBlock.MyGoals>().single().items.single()
        assertEquals("goal_get_started", goal.id)
        assertEquals(null, goal.subtitle)
        assertEquals(ImageRef.FALLBACK, goal.image)
    }

    @Test
    fun unknownTypeAndMalformedKnownBodyBothDegradeWithoutDropping() {
        val manifest = decodeManifest(MIXED_MANIFEST)

        assertEquals(4, manifest.blocks.size, "nothing is dropped")
        assertIs<ManifestBlock.Greeting>(manifest.blocks[0])
        assertEquals(
            2,
            manifest.blocks.count { it is ManifestBlock.Unknown },
            "both the unknown-discriminator AND the malformed-known-body become Unknown",
        )
        assertEquals(ManifestBlock.Unknown, manifest.blocks[1])
        assertEquals(ManifestBlock.Unknown, manifest.blocks[2])
        assertIs<ManifestBlock.Promotion>(manifest.blocks[3])
    }

    @Test
    fun listNestedUnknownActionDegradesWithoutDroppingCarousel() {
        val carousel = decodeManifest(CAROUSEL_UNKNOWN_ACTION_MANIFEST)
            .blocks
            .filterIsInstance<ManifestBlock.ClassCarousel>()
            .single()

        assertEquals(1, carousel.items.size, "the carousel is not dropped")
        assertEquals(
            ManifestAction.Unknown,
            carousel.items.single().actionRef,
            "an unknown list-nested action type degrades to Unknown (coerceInputValues does not save it)",
        )
    }

    @Test
    fun nonObjectBodyYieldsEmptyManifestNotCrash() {
        assertEquals(0, decodeManifest("[]").blocks.size)
        assertEquals(0, decodeManifest("{}").blocks.size)
        assertEquals(0, decodeManifest("not json").blocks.size)
    }

    private companion object {
        val USER1_MANIFEST = """
        {
          "blocks": [
            { "type": "greeting", "title": "Good afternoon, Avid" },
            { "type": "hero", "title": "Crush your goals this week", "subtitle": "1 more to go." },
            { "type": "myClub", "name": "Virgin Active Sea Point", "addressLine": "96 Beach Rd",
              "openingHoursToday": "05:00 – 22:00", "phoneNumber": "+27 21 439 1240" },
            { "type": "classCarousel", "title": "Your week",
              "viewAllAction": { "type": "openTimetable", "clubId": "club_sea_point" },
              "items": [
                { "id": "sp-lunchtime-spin::2026-06-24", "title": "Lunchtime Spin",
                  "subtitle": "Thabo Ndlovu", "imageRef": "spin", "startsAt": "2026-06-24T12:00:00+02:00",
                  "actionLabel": "View",
                  "actionRef": { "type": "openClass", "clubId": "club_sea_point",
                    "classId": "sp-lunchtime-spin::2026-06-24" } }
              ] },
            { "type": "myRewards", "title": "Your rewards",
              "items": [ { "id": "reward_001", "title": "Free smoothie",
                "imageRef": "reward_smoothie", "badge": "Expires Sunday" } ] },
            { "type": "myGoals", "title": "Your goals",
              "items": [ { "id": "goal_001", "title": "Attend 4 classes",
                "subtitle": "3 of 4", "imageRef": "goal_classes" } ] },
            { "type": "promotion", "title": "Summer Shape-Up", "subtitle": "Join 12 classes",
              "imageRef": "promo_summer_challenge" },
            { "type": "experimental", "payload": { "kind": "futureFeature", "title": "Coming soon",
              "data": { "foo": "bar" } } }
          ]
        }
        """.trimIndent()

        val USER2_MANIFEST = """
        {
          "blocks": [
            { "type": "greeting", "title": "Welcome, Competitive" },
            { "type": "hero", "title": "Your journey starts here", "subtitle": "Explore classes." },
            { "type": "myClub", "name": "Virgin Active Aldersgate", "addressLine": "1 Aldersgate St",
              "openingHoursToday": "06:30 – 22:00", "phoneNumber": "+44 20 7600 5130" },
            { "type": "promotion", "title": "Welcome to Virgin Active", "subtitle": "First month bonus",
              "imageRef": "promo_welcome_bonus" },
            { "type": "classCarousel", "title": "Your week",
              "viewAllAction": { "type": "openTimetable", "clubId": "club_aldersgate" },
              "items": [
                { "id": "ag-spin-burn::2026-06-24", "title": "Spin & Burn", "subtitle": "James Fletcher",
                  "imageRef": "spin", "startsAt": "2026-06-24T17:30:00+01:00", "actionLabel": "View",
                  "actionRef": { "type": "openClass", "clubId": "club_aldersgate",
                    "classId": "ag-spin-burn::2026-06-24" } }
              ] },
            { "type": "myGoals", "title": "Your goals",
              "items": [ { "id": "goal_get_started", "title": "Set your first goal" } ] },
            { "type": "experimental", "payload": { "kind": "futureFeature", "title": "Coming soon",
              "data": { "foo": "bar" } } }
          ]
        }
        """.trimIndent()

        val MIXED_MANIFEST = """
        {
          "blocks": [
            { "type": "greeting", "title": "Hi" },
            { "type": "experimental", "payload": { "foo": "bar" } },
            { "type": "hero", "title": 42 },
            { "type": "promotion", "title": "Promo", "imageRef": "promo_summer_challenge" }
          ]
        }
        """.trimIndent()

        val CAROUSEL_UNKNOWN_ACTION_MANIFEST = """
        {
          "blocks": [
            { "type": "classCarousel", "title": "Your week",
              "viewAllAction": { "type": "openTimetable", "clubId": "club_sea_point" },
              "items": [
                { "id": "x::2026-06-24", "title": "Mystery Class", "imageRef": "spin",
                  "startsAt": "2026-06-24T12:00:00+02:00", "actionLabel": "View",
                  "actionRef": { "type": "openSomethingNew", "clubId": "club_sea_point" } }
              ] }
          ]
        }
        """.trimIndent()
    }
}
