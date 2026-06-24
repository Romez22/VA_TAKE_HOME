package com.virginactive.shared.data.home.dto

import com.virginactive.shared.domain.home.ImageRef
import com.virginactive.shared.domain.home.ManifestAction
import com.virginactive.shared.domain.home.ManifestBlock

internal fun ManifestBlockDto.toDomain(): ManifestBlock =
    when (this) {
        is GreetingBlockDto -> ManifestBlock.Greeting(title = title)
        is HeroBlockDto -> ManifestBlock.Hero(title = title, subtitle = subtitle)
        is MyClubBlockDto -> ManifestBlock.MyClub(
            name = name,
            addressLine = addressLine,
            openingHoursToday = openingHoursToday,
            phoneNumber = phoneNumber,
        )
        is ClassCarouselBlockDto -> ManifestBlock.ClassCarousel(
            title = title,
            viewAllAction = viewAllAction.toDomain(),
            items = items.map { it.toDomain() },
        )
        is MyRewardsBlockDto -> ManifestBlock.MyRewards(
            title = title,
            items = items.map { it.toDomain() },
        )
        is MyGoalsBlockDto -> ManifestBlock.MyGoals(
            title = title,
            items = items.map { it.toDomain() },
        )
        is PromotionBlockDto -> ManifestBlock.Promotion(
            title = title,
            subtitle = subtitle,
            image = ImageRef.from(imageRef),
        )
        is UnknownBlockDto -> ManifestBlock.Unknown
    }

private fun ClassCardDto.toDomain(): ManifestBlock.ClassCarousel.Item =
    ManifestBlock.ClassCarousel.Item(
        id = id,
        title = title,
        subtitle = subtitle,
        image = ImageRef.from(imageRef),
        startsAt = startsAt,
        actionLabel = actionLabel,
        actionRef = actionRef.toDomain(),
    )

private fun RewardCardDto.toDomain(): ManifestBlock.MyRewards.Item =
    ManifestBlock.MyRewards.Item(
        id = id,
        title = title,
        subtitle = subtitle,
        image = ImageRef.from(imageRef),
        badge = badge,
    )

private fun GoalCardDto.toDomain(): ManifestBlock.MyGoals.Item =
    ManifestBlock.MyGoals.Item(
        id = id,
        title = title,
        subtitle = subtitle,
        image = ImageRef.from(imageRef),
    )

private fun ActionDto.toDomain(): ManifestAction =
    when (type) {
        "openTimetable" -> ManifestAction.OpenTimetable(clubId = clubId)
        "openClass" -> ManifestAction.OpenClass(clubId = clubId, classId = classId)
        else -> ManifestAction.Unknown
    }
