package org.example.tophits.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tracks")
data class Track(
    @Id
    val id: Long? = null,
    @Column("track_name")
    val trackName: String,
    @Column("artist_name")
    val artistName: String,
    @Column("released_year")
    val releasedYear: Int,
    @Column("released_month")
    val releasedMonth: Int,
    @Column("released_day")
    val releasedDay: Int,
    @Column("in_spotify_playlists")
    val inSpotifyPlaylists: Int = 0,
    @Column("in_spotify_charts")
    val inSpotifyCharts: Int = 0,
    val streams: Long = 0,
    @Column("in_apple_playlists")
    val inApplePlaylists: Int = 0,
    @Column("in_apple_charts")
    val inAppleCharts: Int = 0,
    @Column("in_deezer_playlists")
    val inDeezerPlaylists: Int = 0,
    @Column("in_deezer_charts")
    val inDeezerCharts: Int = 0,
    @Column("in_shazam_charts")
    val inShazamCharts: Int = 0,
    val bpm: Int? = null,
    @Column("key_signature")
    val keySignature: String? = null,
    val mode: String? = null,
    @Column("danceability_percent")
    val danceabilityPercent: Int? = null,
    @Column("valence_percent")
    val valencePercent: Int? = null,
    @Column("energy_percent")
    val energyPercent: Int? = null,
    @Column("acousticness_percent")
    val acousticnessPercent: Int? = null,
    @Column("instrumentalness_percent")
    val instrumentalnessPercent: Int? = null,
    @Column("liveness_percent")
    val livenessPercent: Int? = null,
    @Column("speechiness_percent")
    val speechinessPercent: Int? = null
)