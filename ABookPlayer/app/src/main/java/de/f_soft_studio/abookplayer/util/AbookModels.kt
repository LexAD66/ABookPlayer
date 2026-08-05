package de.f_soft_studio.abookplayer.util

import kotlinx.serialization.Serializable

/**
 * Datenmodelle für .abook Metadaten und Kapitelinformationen.
 * Unterstützt die kanonische manifest.json (Spezifikation v1) und legacy metadata.json.
 */
object AbookModels {

    // --- Kanonisches Format (manifest.json, Format-Version 1) ---

    @Serializable
    data class TrackRaw(
        val file: String,
        val title: String? = null,
        val duration: Long? = null
    )

    @Serializable
    data class AbookManifestRaw(
        val version: Int = 1,
        val id: String? = null,
        val title: String,
        val author: String? = null,
        val narrator: String? = null,
        val description: String? = null,
        val cover: String? = null,
        val series: String? = null,
        val seriesOrder: Int? = null,
        val totalDuration: Long? = null,
        val tracks: List<TrackRaw> = emptyList()
    )

    // --- Legacy Format (metadata.json) ---

    @Serializable
    data class ChapterRaw(
        val file: String,
        val title: String? = null
    )

    @Serializable
    data class AbookMetaRaw(
        val title: String,
        val author: String? = null,
        val description: String? = null,
        val chapters: List<ChapterRaw> = emptyList()
    )

    // --- Interne geparste Modelle ---

    data class Chapter(
        val file: String,
        val title: String
    )

    data class AbookMeta(
        val title: String,
        val author: String,
        val narrator: String? = null,
        val description: String? = null,
        val chapters: List<Chapter>,
        val totalDurationMs: Long = 0L,
        val sourcePath: String
    )
}
