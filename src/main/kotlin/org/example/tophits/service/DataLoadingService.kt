package org.example.tophits.service

import org.example.tophits.model.Track
import org.example.tophits.repository.TrackRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths

@Service
class DataLoadingService(
    private val trackRepository: TrackRepository
) {
    private val logger = LoggerFactory.getLogger(DataLoadingService::class.java)
    private val dataFolderPath = "data"

    fun getAvailableCsvFiles(): List<String> {
        val dataFolder = File(dataFolderPath)
        if (!dataFolder.exists() || !dataFolder.isDirectory) {
            logger.warn("Data folder not found: $dataFolderPath")
            return emptyList()
        }
        
        return dataFolder.listFiles { file ->
            file.isFile && file.extension.lowercase() == "csv"
        }?.map { it.name } ?: emptyList()
    }

    fun loadCsvFile(fileName: String): LoadResult {
        val filePath = Paths.get(dataFolderPath, fileName).toString()
        val file = File(filePath)
        
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $fileName")
        }

        logger.info("Starting to load CSV file: $fileName")
        var successCount = 0
        var errorCount = 0
        val errors = mutableListOf<String>()

        try {
            file.bufferedReader().use { reader ->
                val headerLine = reader.readLine()
                if (headerLine == null) {
                    throw IllegalArgumentException("CSV file is empty")
                }
                
                val headers = headerLine.split(",").map { it.trim() }
                logger.info("CSV headers: $headers")
                
                reader.lineSequence().forEachIndexed { index, line ->
                    try {
                        val values = parseCsvLine(line)
                        if (values.size != headers.size) {
                            logger.warn("Line ${index + 2}: Expected ${headers.size} columns, got ${values.size}")
                            errorCount++
                            errors.add("Line ${index + 2}: Column count mismatch")
                            return@forEachIndexed
                        }
                        
                        val track = createTrackFromCsvData(headers, values)
                        val savedTrack = trackRepository.save(track)
                        logger.info("Loaded track with ID: ${savedTrack.id}")
                        successCount++
                        
                    } catch (e: Exception) {
                        logger.error("Error processing line ${index + 2}: ${e.message}")
                        errorCount++
                        errors.add("Line ${index + 2}: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error reading CSV file: ${e.message}")
            throw RuntimeException("Failed to load CSV file: ${e.message}", e)
        }

        logger.info("CSV loading completed. Success: $successCount, Errors: $errorCount")
        return LoadResult(successCount, errorCount, errors)
    }

    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' && (i == 0 || line[i-1] == ',') -> {
                    inQuotes = true
                }
                char == '"' && inQuotes && (i == line.length - 1 || line[i+1] == ',') -> {
                    inQuotes = false
                }
                char == ',' && !inQuotes -> {
                    values.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }
        values.add(current.toString().trim())
        return values
    }

    private fun createTrackFromCsvData(headers: List<String>, values: List<String>): Track {
        val dataMap = headers.zip(values).toMap()
        
        return Track(
            trackName = dataMap["track_name"] ?: throw IllegalArgumentException("Missing track_name"),
            artistName = dataMap["artist(s)_name"] ?: throw IllegalArgumentException("Missing artist(s)_name"),
            releasedYear = dataMap["released_year"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid released_year"),
            releasedMonth = dataMap["released_month"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid released_month"),
            releasedDay = dataMap["released_day"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid released_day"),
            inSpotifyPlaylists = dataMap["in_spotify_playlists"]?.toIntOrNull() ?: 0,
            inSpotifyCharts = dataMap["in_spotify_charts"]?.toIntOrNull() ?: 0,
            streams = dataMap["streams"]?.toLongOrNull() ?: 0,
            inApplePlaylists = dataMap["in_apple_playlists"]?.toIntOrNull() ?: 0,
            inAppleCharts = dataMap["in_apple_charts"]?.toIntOrNull() ?: 0,
            inDeezerPlaylists = dataMap["in_deezer_playlists"]?.toIntOrNull() ?: 0,
            inDeezerCharts = dataMap["in_deezer_charts"]?.toIntOrNull() ?: 0,
            inShazamCharts = dataMap["in_shazam_charts"]?.toIntOrNull() ?: 0,
            bpm = dataMap["bpm"]?.toIntOrNull(),
            keySignature = dataMap["key"]?.takeIf { it.isNotBlank() },
            mode = dataMap["mode"]?.takeIf { it.isNotBlank() },
            danceabilityPercent = dataMap["danceability_%"]?.toIntOrNull(),
            valencePercent = dataMap["valence_%"]?.toIntOrNull(),
            energyPercent = dataMap["energy_%"]?.toIntOrNull(),
            acousticnessPercent = dataMap["acousticness_%"]?.toIntOrNull(),
            instrumentalnessPercent = dataMap["instrumentalness_%"]?.toIntOrNull(),
            livenessPercent = dataMap["liveness_%"]?.toIntOrNull(),
            speechinessPercent = dataMap["speechiness_%"]?.toIntOrNull()
        )
    }

    fun testDatabaseInsertion(): Track {
        val testTrack = Track(
            trackName = "Test Track",
            artistName = "Test Artist",
            releasedYear = 2023,
            releasedMonth = 1,
            releasedDay = 1,
            streams = 1000000
        )
        
        return trackRepository.save(testTrack)
    }

    data class LoadResult(
        val successCount: Int,
        val errorCount: Int,
        val errors: List<String>
    )
}