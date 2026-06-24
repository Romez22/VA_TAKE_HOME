package com.virginactive.android.image

import androidx.annotation.DrawableRes
import com.virginactive.android.R
import com.virginactive.shared.domain.home.ImageRef


@DrawableRes
fun ImageRef.toDrawableRes(): Int = when (this) {
    ImageRef.CLASS_SPIN -> R.drawable.class_generic
    ImageRef.CLASS_YOGA -> R.drawable.class_generic
    ImageRef.CLASS_HIIT -> R.drawable.class_generic
    ImageRef.CLASS_GROUP_WORKOUT -> R.drawable.class_generic
    ImageRef.CLASS_PILATES -> R.drawable.class_generic

    ImageRef.REWARD_SMOOTHIE -> R.drawable.reward_tile
    ImageRef.REWARD_RETAIL -> R.drawable.reward_tile
    ImageRef.REWARD_GUEST -> R.drawable.reward_tile

    ImageRef.GOAL_CLASSES -> R.drawable.goal_tile
    ImageRef.GOAL_SPIN -> R.drawable.goal_tile
    ImageRef.GOAL_EXPLORE -> R.drawable.goal_tile

    ImageRef.PROMO_SUMMER_CHALLENGE -> R.drawable.promo_tile
    ImageRef.PROMO_WELCOME_BONUS -> R.drawable.promo_tile

    ImageRef.FALLBACK -> R.drawable.ic_fallback_tile
}

@DrawableRes
fun imageRefDrawable(wire: String?): Int = ImageRef.from(wire).toDrawableRes()
