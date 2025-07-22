package org.example.tophits.service

import org.example.tophits.model.BassLineCache
import org.example.tophits.model.Track
import org.example.tophits.repository.BassLineCacheRepository
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BassLineService(
    private val chatClient: ChatClient,
    private val bassLineCacheRepository: BassLineCacheRepository
) {
    private val logger = LoggerFactory.getLogger(BassLineService::class.java)

    fun generateBassLineTabs(track: Track): String {
        logger.info("Generating bass line tabs for track: ${track.trackName} by ${track.artistName}")
        
        // Check if we have cached results for this track using artist name + track name
        val cachedResult = bassLineCacheRepository.findByArtistNameAndTrackName(track.artistName, track.trackName)
        
        if (cachedResult.isPresent) {
            logger.info("Found cached bass line tabs for track: ${track.trackName} by ${track.artistName}")
            return cachedResult.get().bassLineContent
        }
        
        // No cached result found, generate new bass line tabs
        logger.info("No cached result found, calling OpenAI API for track: ${track.trackName} by ${track.artistName}")
        val prompt = buildPrompt(track)
        
        return try {
            val response = chatClient.prompt()
                .user(prompt)
                .call()
                .content()
            
            val bassLineContent = response ?: "Sorry, I couldn't generate bass line tabs for this track."
            
            // Cache the successful result
            if (response != null) {
                try {
                    val cacheEntry = BassLineCache(
                        artistName = track.artistName,
                        trackName = track.trackName,
                        bassLineContent = bassLineContent,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    bassLineCacheRepository.save(cacheEntry)
                    logger.info("Successfully cached bass line tabs for track: ${track.trackName} by ${track.artistName}")
                } catch (cacheException: Exception) {
                    logger.warn("Failed to cache bass line tabs for track: ${track.trackName} by ${track.artistName}", cacheException)
                    // Continue execution even if caching fails
                }
            }
            
            logger.info("Successfully generated bass line tabs for track: ${track.trackName}")
            bassLineContent
            
        } catch (e: Exception) {
            logger.error("Error generating bass line tabs for track: ${track.trackName}", e)
            "Sorry, there was an error generating bass line tabs. Please try again later."
        }
    }
    
    private fun buildPrompt(track: Track): String {
        val trackInfo = StringBuilder().apply {
            append("Generate bass line tabs for the song \"${track.trackName}\" by ${track.artistName}.")
            append(" Released in ${track.releasedYear}.")
            
            track.bpm?.let { append(" BPM: $it.") }
            track.keySignature?.let { append(" Key: $it.") }
            track.mode?.let { append(" Mode: $it.") }
            
            // Add musical characteristics if available
            track.danceabilityPercent?.let { append(" Danceability: $it%.") }
            track.energyPercent?.let { append(" Energy: $it%.") }
            track.valencePercent?.let { append(" Valence: $it%.") }
            
            append("\n\nPlease provide:")
            append("\n1. A simple bass line pattern in tablature format (4-string bass)")
            append("\n2. Chord progression suggestions")
            append("\n3. Playing tips and techniques")
            append("\n4. Rhythm pattern recommendations")
            append("\n\nFormat the response in a clear, readable way with proper sections.")
        }
        
        return trackInfo.toString()
    }
}