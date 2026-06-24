package com.virginactive.shared.domain.model

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val membershipTier: MembershipTier,
    val homeClub: Club,
)
