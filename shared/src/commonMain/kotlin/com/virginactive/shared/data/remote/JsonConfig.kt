package com.virginactive.shared.data.remote

import com.virginactive.shared.data.home.dto.ClassCarouselBlockDto
import com.virginactive.shared.data.home.dto.GreetingBlockDto
import com.virginactive.shared.data.home.dto.HeroBlockDto
import com.virginactive.shared.data.home.dto.ManifestBlockDto
import com.virginactive.shared.data.home.dto.MyClubBlockDto
import com.virginactive.shared.data.home.dto.MyGoalsBlockDto
import com.virginactive.shared.data.home.dto.MyRewardsBlockDto
import com.virginactive.shared.data.home.dto.PromotionBlockDto
import com.virginactive.shared.data.home.dto.UnknownBlockDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val appJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
    isLenient = false
    serializersModule = SerializersModule {
        polymorphic(ManifestBlockDto::class) {
            subclass(GreetingBlockDto::class, GreetingBlockDto.serializer())
            subclass(HeroBlockDto::class, HeroBlockDto.serializer())
            subclass(MyClubBlockDto::class, MyClubBlockDto.serializer())
            subclass(ClassCarouselBlockDto::class, ClassCarouselBlockDto.serializer())
            subclass(MyRewardsBlockDto::class, MyRewardsBlockDto.serializer())
            subclass(MyGoalsBlockDto::class, MyGoalsBlockDto.serializer())
            subclass(PromotionBlockDto::class, PromotionBlockDto.serializer())
            defaultDeserializer { UnknownBlockDto.serializer() }
        }
    }
}
