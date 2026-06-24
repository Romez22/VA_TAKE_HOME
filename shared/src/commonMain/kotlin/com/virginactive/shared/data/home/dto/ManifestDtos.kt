package com.virginactive.shared.data.home.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
internal sealed interface ManifestBlockDto

@Serializable
@SerialName("greeting")
internal data class GreetingBlockDto(
    val title: String,
) : ManifestBlockDto

@Serializable
@SerialName("hero")
internal data class HeroBlockDto(
    val title: String,
    val subtitle: String? = null,
) : ManifestBlockDto

@Serializable
@SerialName("myClub")
internal data class MyClubBlockDto(
    val name: String,
    val addressLine: String? = null,
    val openingHoursToday: String? = null,
    val phoneNumber: String? = null,
) : ManifestBlockDto

@Serializable
@SerialName("classCarousel")
internal data class ClassCarouselBlockDto(
    val title: String,
    val viewAllAction: ActionDto,
    val items: List<ClassCardDto>,
) : ManifestBlockDto

@Serializable
internal data class ClassCardDto(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageRef: String? = null,
    val startsAt: String,
    val actionLabel: String? = null,
    val actionRef: ActionDto,
)

@Serializable
@SerialName("myRewards")
internal data class MyRewardsBlockDto(
    val title: String,
    val items: List<RewardCardDto>,
) : ManifestBlockDto

@Serializable
internal data class RewardCardDto(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageRef: String? = null,
    val badge: String? = null,
)

@Serializable
@SerialName("myGoals")
internal data class MyGoalsBlockDto(
    val title: String,
    val items: List<GoalCardDto>,
) : ManifestBlockDto

@Serializable
internal data class GoalCardDto(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageRef: String? = null,
)

@Serializable
@SerialName("promotion")
internal data class PromotionBlockDto(
    val title: String,
    val subtitle: String? = null,
    val imageRef: String? = null,
) : ManifestBlockDto

@Serializable
internal data object UnknownBlockDto : ManifestBlockDto

@Serializable
internal data class ActionDto(
    val type: String,
    val clubId: String? = null,
    val classId: String? = null,
)
