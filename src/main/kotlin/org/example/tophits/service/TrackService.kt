package org.example.tophits.service

import org.example.tophits.model.Track
import org.example.tophits.repository.TrackRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class TrackService(
    private val trackRepository: TrackRepository
) {
    private val logger = LoggerFactory.getLogger(TrackService::class.java)

    fun getAllTracks(page: Int = 0, size: Int = 20, sortBy: String = "id", sortDirection: String = "asc"): Page<Track> {
        logger.info("Fetching tracks - page: $page, size: $size, sortBy: $sortBy, sortDirection: $sortDirection")
        
        val sort = if (sortDirection.lowercase() == "desc") {
            Sort.by(sortBy).descending()
        } else {
            Sort.by(sortBy).ascending()
        }
        
        val pageable: Pageable = PageRequest.of(page, size, sort)
        return trackRepository.findAll(pageable)
    }

    fun searchTracks(
        query: String,
        searchType: String = "track",
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "id",
        sortDirection: String = "asc"
    ): Page<Track> {
        logger.info("Searching tracks - query: $query, searchType: $searchType, page: $page, size: $size")
        
        val sort = if (sortDirection.lowercase() == "desc") {
            Sort.by(sortBy).descending()
        } else {
            Sort.by(sortBy).ascending()
        }
        
        val pageable: Pageable = PageRequest.of(page, size, sort)
        
        return when (searchType.lowercase()) {
            "artist" -> trackRepository.findByArtistNameContainingIgnoreCase(query, pageable)
            "track" -> trackRepository.findByTrackNameContainingIgnoreCase(query, pageable)
            else -> trackRepository.findByTrackNameContainingIgnoreCase(query, pageable)
        }
    }

    fun getTrackById(id: Long): Track? {
        logger.info("Fetching track by ID: $id")
        return trackRepository.findById(id).orElse(null)
    }

    fun getTotalTracksCount(): Long {
        return trackRepository.count()
    }
}