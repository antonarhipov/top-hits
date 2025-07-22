package org.example.tophits.repository

import org.example.tophits.model.Track
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : CrudRepository<Track, Long> {
    fun findByTrackNameContainingIgnoreCase(trackName: String): List<Track>
    fun findByArtistNameContainingIgnoreCase(artistName: String): List<Track>
    fun countByReleasedYear(year: Int): Long
}