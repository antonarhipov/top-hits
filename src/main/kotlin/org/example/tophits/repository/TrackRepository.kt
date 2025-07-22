package org.example.tophits.repository

import org.example.tophits.model.Track
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : CrudRepository<Track, Long>, PagingAndSortingRepository<Track, Long> {
    fun findByTrackNameContainingIgnoreCase(trackName: String): List<Track>
    fun findByArtistNameContainingIgnoreCase(artistName: String): List<Track>
    fun countByReleasedYear(year: Int): Long
    
    // Paginated methods
    override fun findAll(pageable: Pageable): Page<Track>
    fun findByTrackNameContainingIgnoreCase(trackName: String, pageable: Pageable): Page<Track>
    fun findByArtistNameContainingIgnoreCase(artistName: String, pageable: Pageable): Page<Track>
}