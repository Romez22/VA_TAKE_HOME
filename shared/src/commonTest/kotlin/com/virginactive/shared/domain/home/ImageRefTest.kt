package com.virginactive.shared.domain.home

import kotlin.test.Test
import kotlin.test.assertEquals

class ImageRefTest {

    @Test
    fun knownImageRefsMapToTheirLogicalKey() {
        assertEquals(ImageRef.CLASS_SPIN, ImageRef.from("spin"))
        assertEquals(ImageRef.CLASS_YOGA, ImageRef.from("yoga"))
        assertEquals(ImageRef.CLASS_HIIT, ImageRef.from("hiit"))
        assertEquals(ImageRef.CLASS_GROUP_WORKOUT, ImageRef.from("groupWorkout"))
        assertEquals(ImageRef.CLASS_PILATES, ImageRef.from("pilates"))
        assertEquals(ImageRef.REWARD_SMOOTHIE, ImageRef.from("reward_smoothie"))
        assertEquals(ImageRef.REWARD_RETAIL, ImageRef.from("reward_retail"))
        assertEquals(ImageRef.REWARD_GUEST, ImageRef.from("reward_guest"))
        assertEquals(ImageRef.GOAL_CLASSES, ImageRef.from("goal_classes"))
        assertEquals(ImageRef.GOAL_SPIN, ImageRef.from("goal_spin"))
        assertEquals(ImageRef.GOAL_EXPLORE, ImageRef.from("goal_explore"))
        assertEquals(ImageRef.PROMO_SUMMER_CHALLENGE, ImageRef.from("promo_summer_challenge"))
        assertEquals(ImageRef.PROMO_WELCOME_BONUS, ImageRef.from("promo_welcome_bonus"))
    }

    @Test
    fun unmappedStringMapsToFallback() {
        assertEquals(ImageRef.FALLBACK, ImageRef.from("some_future_asset"))
        assertEquals(ImageRef.FALLBACK, ImageRef.from(""))
    }

    @Test
    fun absentRefMapsToFallback() {
        assertEquals(ImageRef.FALLBACK, ImageRef.from(null))
    }
}
