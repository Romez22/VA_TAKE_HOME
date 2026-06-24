package com.virginactive.shared.data.auth.dto

import com.virginactive.shared.domain.model.Club
import com.virginactive.shared.domain.model.MembershipTier
import com.virginactive.shared.domain.model.UserProfile


internal fun UserProfileDto.toDomain(): UserProfile =
    UserProfile(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        membershipTier = membershipTier.toMembershipTier(),
        homeClub = homeClub.toDomain(),
    )

internal fun ClubSummaryDto.toDomain(): Club =
    Club(id = id, name = name)

private fun String.toMembershipTier(): MembershipTier =
    MembershipTier.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: MembershipTier.UNKNOWN
