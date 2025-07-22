package org.example.tophits.service

import org.example.tophits.controller.TrackController.MidiNote
import org.example.tophits.model.BassLineCache
import org.example.tophits.model.Track
import org.example.tophits.repository.BassLineCacheRepository
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@Service
class BassLineService(
    private val chatClient: ChatClient,
    private val bassLineCacheRepository: BassLineCacheRepository
) {
    private val logger = LoggerFactory.getLogger(BassLineService::class.java)
    private val objectMapper = ObjectMapper()

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

    fun extractMidiNotes(track: Track): List<MidiNote> {
        logger.info("Extracting MIDI notes for track: ${track.trackName} by ${track.artistName}")

        val prompt = buildMidiPrompt(track)

        return try {
            val response = chatClient.prompt()
                .user(prompt)
                .call()
                .content()

            logger.debug("Response for midi notes: $response")

            if (response != null) {
                parseMidiNotesFromResponse(response)
            } else {
                logger.warn("No response received for MIDI notes extraction")
                emptyList()
            }

        } catch (e: Exception) {
            logger.error("Error extracting MIDI notes for track: ${track.trackName}", e)
            emptyList()
        }
    }

    private fun buildMidiPrompt(track: Track): String {
        val trackInfo = StringBuilder().apply {
            append("Extract MIDI notes for a bass line for the song \"${track.trackName}\" by ${track.artistName}.")
            append(" Released in ${track.releasedYear}.")

            track.bpm?.let { append(" BPM: $it.") }
            track.keySignature?.let { append(" Key: $it.") }
            track.mode?.let { append(" Mode: $it.") }

            // Add musical characteristics if available
            track.danceabilityPercent?.let { append(" Danceability: $it%.") }
            track.energyPercent?.let { append(" Energy: $it%.") }
            track.valencePercent?.let { append(" Valence: $it%.") }

            append("\n\nPlease provide a simple bass line pattern as MIDI notes in JSON format.")
            append("\nReturn ONLY a JSON array of objects with the following structure:")
            append("\n[")
            append("\n  {\"note\": \"E\", \"octave\": 2, \"duration\": 1.0, \"startTime\": 0.0},")
            append("\n  {\"note\": \"A\", \"octave\": 2, \"duration\": 1.0, \"startTime\": 1.0}")
            append("\n]")
            append("\n\nWhere:")
            append("\n- note: Note name (C, C#, D, D#, E, F, F#, G, G#, A, A#, B)")
            append("\n- octave: Octave number (typically 1-4 for bass)")
            append("\n- duration: Note duration in beats (1.0 = quarter note, 0.5 = eighth note, 2.0 = half note)")
            append("\n- startTime: When the note starts in beats from the beginning")
            append("\n\nCreate a 4-8 measure bass line pattern that fits the song's style and key.")
            append("\nReturn ONLY the JSON array, no additional text or explanation.")
        }

        return trackInfo.toString()
    }
    
    private fun parseMidiNotesFromResponse(response: String): List<MidiNote> {
        return try {
            // Clean the response to extract just the JSON array
            val jsonStart = response.indexOf('[')
            val jsonEnd = response.lastIndexOf(']')
            
            if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
                logger.warn("No valid JSON array found in response")
                return emptyList()
            }
            
            val jsonString = response.substring(jsonStart, jsonEnd + 1)
            
            // Parse JSON array into list of maps
            val notesList: List<Map<String, Any>> = objectMapper.readValue(jsonString)
            
            // Convert to MidiNote objects
            notesList.mapNotNull { noteMap ->
                try {
                    MidiNote(
                        note = noteMap["note"] as String,
                        octave = (noteMap["octave"] as Number).toInt(),
                        duration = (noteMap["duration"] as Number).toDouble(),
                        startTime = (noteMap["startTime"] as Number).toDouble()
                    )
                } catch (e: Exception) {
                    logger.warn("Failed to parse MIDI note: $noteMap", e)
                    null
                }
            }
            
        } catch (e: Exception) {
            logger.error("Error parsing MIDI notes from response: $response", e)
            emptyList()
        }
    }

    /**
     * Extracts notes from cached bass line tabs for a given track.
     * This method retrieves cached bass line content and extracts notes from the tablature.
     * 
     * @param track The track to extract notes for
     * @return List of MidiNote objects extracted from cached tablature, or empty list if no cache exists
     */
    fun extractNotesFromCachedTablature(track: Track): List<MidiNote> {
        logger.info("Extracting notes from cached tablature for track: ${track.trackName} by ${track.artistName}")
        
        return try {
            // Check if we have cached bass line content
            val cachedResult = bassLineCacheRepository.findByArtistNameAndTrackName(track.artistName, track.trackName)
            
            if (cachedResult.isPresent) {
                val bassLineContent = cachedResult.get().bassLineContent
                logger.info("Found cached bass line content, extracting notes")
                extractNotesFromTablature(bassLineContent)
            } else {
                logger.info("No cached bass line content found for track: ${track.trackName} by ${track.artistName}")
                emptyList()
            }
            
        } catch (e: Exception) {
            logger.error("Error extracting notes from cached tablature for track: ${track.trackName}", e)
            emptyList()
        }
    }

    /**
     * Extracts notes from bass tablature content.
     * Parses standard 4-string bass tablature format and converts fret positions to note names.
     * 
     * @param bassLineContent The tablature content containing bass tabs
     * @return List of MidiNote objects extracted from the tablature
     */
    fun extractNotesFromTablature(bassLineContent: String): List<MidiNote> {
        logger.info("Extracting notes from bass tablature")
        
        return try {
            val notes = mutableListOf<MidiNote>()
            val lines = bassLineContent.lines()
            
            // Find tablature section - look for lines that contain string indicators (G, D, A, E)
            val tabLines = findTablatureLines(lines)
            
            if (tabLines.isEmpty()) {
                logger.warn("No tablature lines found in content")
                return emptyList()
            }
            
            // Parse each set of tablature lines
            val tabSections = groupTablatureLines(tabLines)
            
            for (section in tabSections) {
                val sectionNotes = parseTabSection(section)
                notes.addAll(sectionNotes)
            }
            
            logger.info("Successfully extracted ${notes.size} notes from tablature")
            notes.toList()
            
        } catch (e: Exception) {
            logger.error("Error extracting notes from tablature", e)
            emptyList()
        }
    }
    
    private fun findTablatureLines(lines: List<String>): List<String> {
        val tabLines = mutableListOf<String>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            // Look for lines that start with string indicators or contain fret numbers
            if (isTabLine(trimmedLine)) {
                tabLines.add(trimmedLine)
            }
        }
        
        return tabLines
    }
    
    private fun isTabLine(line: String): Boolean {
        // Check if line starts with bass string indicators
        val startsWithString = line.matches(Regex("^[GDAE]\\s*[|:-].*")) ||
                              line.matches(Regex("^[GDAE]\\s*\\d.*"))
        
        // Check if line contains fret numbers and dashes/pipes (common tab formatting)
        val containsTabFormat = line.contains(Regex("[0-9]")) && 
                               (line.contains("-") || line.contains("|"))
        
        // Check if it's a pure tab line with numbers and dashes
        val isPureTab = line.matches(Regex(".*[0-9-|]+.*")) && 
                       line.length > 5 && 
                       !line.contains(Regex("[a-zA-Z]{3,}")) // Avoid lines with long words
        
        return startsWithString || containsTabFormat || isPureTab
    }
    
    private fun groupTablatureLines(tabLines: List<String>): List<List<String>> {
        val sections = mutableListOf<List<String>>()
        var currentSection = mutableListOf<String>()
        
        for (line in tabLines) {
            if (line.trim().isEmpty()) {
                if (currentSection.isNotEmpty()) {
                    sections.add(currentSection.toList())
                    currentSection.clear()
                }
            } else {
                currentSection.add(line)
            }
        }
        
        if (currentSection.isNotEmpty()) {
            sections.add(currentSection.toList())
        }
        
        return sections
    }
    
    private fun parseTabSection(section: List<String>): List<MidiNote> {
        val notes = mutableListOf<MidiNote>()
        
        // Standard bass tuning (from highest to lowest pitch)
        val stringTuning = mapOf(
            0 to Pair("G", 3),  // G string, 3rd octave
            1 to Pair("D", 3),  // D string, 3rd octave  
            2 to Pair("A", 2),  // A string, 2nd octave
            3 to Pair("E", 2)   // E string, 2nd octave
        )
        
        // Try to identify which lines correspond to which strings
        val stringLines = identifyStringLines(section)
        
        var currentTime = 0.0
        val defaultDuration = 0.5 // Default to eighth note
        
        // Find the maximum length to iterate through all positions
        val maxLength = stringLines.values.maxOfOrNull { it.length } ?: 0
        
        for (position in 0 until maxLength) {
            for ((stringIndex, line) in stringLines) {
                if (position < line.length) {
                    val char = line[position]
                    if (char.isDigit()) {
                        val fret = char.toString().toInt()
                        val (rootNote, baseOctave) = stringTuning[stringIndex] ?: continue
                        
                        val (noteName, octave) = calculateNoteFromFret(rootNote, baseOctave, fret)
                        
                        notes.add(MidiNote(
                            note = noteName,
                            octave = octave,
                            duration = defaultDuration,
                            startTime = currentTime
                        ))
                    }
                }
            }
            currentTime += defaultDuration
        }
        
        return notes
    }
    
    private fun identifyStringLines(section: List<String>): Map<Int, String> {
        val stringLines = mutableMapOf<Int, String>()
        
        for ((index, line) in section.withIndex()) {
            val trimmedLine = line.trim()
            
            // Try to identify string by prefix
            when {
                trimmedLine.startsWith("G") -> stringLines[0] = extractTabContent(trimmedLine)
                trimmedLine.startsWith("D") -> stringLines[1] = extractTabContent(trimmedLine)
                trimmedLine.startsWith("A") -> stringLines[2] = extractTabContent(trimmedLine)
                trimmedLine.startsWith("E") -> stringLines[3] = extractTabContent(trimmedLine)
                else -> {
                    // If no clear string identifier, assign by position (assuming standard order)
                    if (index < 4) {
                        stringLines[index] = trimmedLine
                    }
                }
            }
        }
        
        return stringLines
    }
    
    private fun extractTabContent(line: String): String {
        // Remove string identifier and common separators, keep the tab content
        return line.replace(Regex("^[GDAE]\\s*[|:-]*\\s*"), "")
    }
    
    private fun calculateNoteFromFret(rootNote: String, baseOctave: Int, fret: Int): Pair<String, Int> {
        val chromaticScale = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val rootIndex = chromaticScale.indexOf(rootNote)
        
        if (rootIndex == -1) {
            return Pair(rootNote, baseOctave) // Fallback
        }
        
        val totalSemitones = rootIndex + fret
        val noteIndex = totalSemitones % 12
        val octaveOffset = totalSemitones / 12
        
        return Pair(chromaticScale[noteIndex], baseOctave + octaveOffset)
    }
}