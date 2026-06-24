package com.virginactive.shared.domain.home

enum class ImageRef(private val wireKey: String?) {
    CLASS_SPIN("spin"),
    CLASS_YOGA("yoga"),
    CLASS_HIIT("hiit"),
    CLASS_GROUP_WORKOUT("groupWorkout"),
    CLASS_PILATES("pilates"),

    REWARD_SMOOTHIE("reward_smoothie"),
    REWARD_RETAIL("reward_retail"),
    REWARD_GUEST("reward_guest"),

    GOAL_CLASSES("goal_classes"),
    GOAL_SPIN("goal_spin"),
    GOAL_EXPLORE("goal_explore"),

    PROMO_SUMMER_CHALLENGE("promo_summer_challenge"),
    PROMO_WELCOME_BONUS("promo_welcome_bonus"),

    FALLBACK(null);

    companion object {
        fun from(imageRef: String?): ImageRef =
            entries.firstOrNull { it.wireKey != null && it.wireKey == imageRef } ?: FALLBACK
    }
}
