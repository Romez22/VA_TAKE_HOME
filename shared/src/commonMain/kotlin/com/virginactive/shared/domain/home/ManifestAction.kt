package com.virginactive.shared.domain.home

sealed interface ManifestAction {

    data class OpenTimetable(
        val clubId: String? = null,
    ) : ManifestAction

    data class OpenClass(
        val clubId: String? = null,
        val classId: String? = null,
    ) : ManifestAction

    data object Unknown : ManifestAction
}
