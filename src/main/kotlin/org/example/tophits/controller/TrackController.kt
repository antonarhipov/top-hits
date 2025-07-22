package org.example.tophits.controller

import org.example.tophits.model.Track
import org.example.tophits.service.TrackService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/tracks")
class TrackController(
    private val trackService: TrackService
) {
    private val logger = LoggerFactory.getLogger(TrackController::class.java)

    @GetMapping
    fun tracksPage(model: Model): String {
        logger.info("Serving tracks page")
        val totalCount = trackService.getTotalTracksCount()
        model.addAttribute("totalCount", totalCount)
        return "tracks"
    }

    @GetMapping("/api")
    @ResponseBody
    fun getTracksApi(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDirection: String,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "track") searchType: String
    ): ResponseEntity<TracksResponse> {
        logger.info("API request - page: $page, size: $size, search: $search")
        
        return try {
            val tracksPage = if (search.isNullOrBlank()) {
                trackService.getAllTracks(page, size, sortBy, sortDirection)
            } else {
                trackService.searchTracks(search, searchType, page, size, sortBy, sortDirection)
            }
            
            val response = TracksResponse(
                tracks = tracksPage.content,
                currentPage = tracksPage.number,
                totalPages = tracksPage.totalPages,
                totalElements = tracksPage.totalElements,
                size = tracksPage.size,
                hasNext = tracksPage.hasNext(),
                hasPrevious = tracksPage.hasPrevious(),
                isFirst = tracksPage.isFirst,
                isLast = tracksPage.isLast
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error fetching tracks: ${e.message}", e)
            ResponseEntity.badRequest().body(
                TracksResponse(
                    tracks = emptyList(),
                    currentPage = 0,
                    totalPages = 0,
                    totalElements = 0,
                    size = 0,
                    hasNext = false,
                    hasPrevious = false,
                    isFirst = true,
                    isLast = true,
                    error = e.message
                )
            )
        }
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    fun getTrackById(@PathVariable id: Long): ResponseEntity<Track> {
        logger.info("Fetching track by ID: $id")
        
        val track = trackService.getTrackById(id)
        return if (track != null) {
            ResponseEntity.ok(track)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/search")
    @ResponseBody
    fun searchTracks(
        @RequestParam query: String,
        @RequestParam(defaultValue = "track") searchType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDirection: String
    ): ResponseEntity<TracksResponse> {
        logger.info("Search request - query: $query, searchType: $searchType, page: $page")
        
        return try {
            val tracksPage = trackService.searchTracks(query, searchType, page, size, sortBy, sortDirection)
            
            val response = TracksResponse(
                tracks = tracksPage.content,
                currentPage = tracksPage.number,
                totalPages = tracksPage.totalPages,
                totalElements = tracksPage.totalElements,
                size = tracksPage.size,
                hasNext = tracksPage.hasNext(),
                hasPrevious = tracksPage.hasPrevious(),
                isFirst = tracksPage.isFirst,
                isLast = tracksPage.isLast
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error searching tracks: ${e.message}", e)
            ResponseEntity.badRequest().body(
                TracksResponse(
                    tracks = emptyList(),
                    currentPage = 0,
                    totalPages = 0,
                    totalElements = 0,
                    size = 0,
                    hasNext = false,
                    hasPrevious = false,
                    isFirst = true,
                    isLast = true,
                    error = e.message
                )
            )
        }
    }

    data class TracksResponse(
        val tracks: List<Track>,
        val currentPage: Int,
        val totalPages: Int,
        val totalElements: Long,
        val size: Int,
        val hasNext: Boolean,
        val hasPrevious: Boolean,
        val isFirst: Boolean,
        val isLast: Boolean,
        val error: String? = null
    )
}