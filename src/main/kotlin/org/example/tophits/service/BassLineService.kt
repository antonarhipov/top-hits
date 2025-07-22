package org.example.tophits.service

import org.example.tophits.model.Track
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class BassLineService(
    private val chatClient: ChatClient
) {
    private val logger = LoggerFactory.getLogger(BassLineService::class.java)

    fun generateBassLineTabs(track: Track): String {
        logger.info("Generating bass line tabs for track: ${track.trackName} by ${track.artistName}")
        
        val prompt = buildPrompt(track)
        
        return try {
            val response = chatClient.prompt()
                .user(prompt)
                .call()
                .content()
            
            logger.info("Successfully generated bass line tabs for track: ${track.trackName}")
            response ?: "Sorry, I couldn't generate bass line tabs for this track."
            
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