package com.virginactive.shared.domain.home

sealed interface ManifestBlock {

    data class Greeting(
        val title: String,
    ) : ManifestBlock

    data class Hero(
        val title: String,
        val subtitle: String? = null,
    ) : ManifestBlock

    data class MyClub(
        val name: String,
        val addressLine: String? = null,
        val openingHoursToday: String? = null,
        val phoneNumber: String? = null,
    ) : ManifestBlock

    data class ClassCarousel(
        val title: String,
        val viewAllAction: ManifestAction,
        val items: List<Item>,
    ) : ManifestBlock {
        data class Item(
            val id: String,
            val title: String,
            val subtitle: String? = null,
            val image: ImageRef,
            val startsAt: String,
            val actionLabel: String? = null,
            val actionRef: ManifestAction,
        )
    }

    data class MyRewards(
        val title: String,
        val items: List<Item>,
    ) : ManifestBlock {
        data class Item(
            val id: String,
            val title: String,
            val subtitle: String? = null,
            val image: ImageRef,
            val badge: String? = null,
        )
    }

    data class MyGoals(
        val title: String,
        val items: List<Item>,
    ) : ManifestBlock {
        data class Item(
            val id: String,
            val title: String,
            val subtitle: String? = null,
            val image: ImageRef,
        )
    }

    data class Promotion(
        val title: String,
        val subtitle: String? = null,
        val image: ImageRef,
    ) : ManifestBlock

    data object Unknown : ManifestBlock
}
