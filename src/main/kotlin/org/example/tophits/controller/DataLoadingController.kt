package org.example.tophits.controller

import org.example.tophits.service.DataLoadingService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/data")
class DataLoadingController(
    private val dataLoadingService: DataLoadingService
) {
    private val logger = LoggerFactory.getLogger(DataLoadingController::class.java)

    @GetMapping("/load")
    fun loadDataPage(model: Model): String {
        val csvFiles = dataLoadingService.getAvailableCsvFiles()
        model.addAttribute("csvFiles", csvFiles)
        return "data-load"
    }

    @GetMapping("/files")
    @ResponseBody
    fun getAvailableFiles(): ResponseEntity<List<String>> {
        val files = dataLoadingService.getAvailableCsvFiles()
        return ResponseEntity.ok(files)
    }

    @PostMapping("/load/{fileName}")
    @ResponseBody
    fun loadCsvFile(@PathVariable fileName: String): ResponseEntity<LoadResponse> {
        return try {
            logger.info("Received request to load file: $fileName")
            val result = dataLoadingService.loadCsvFile(fileName)
            
            val response = LoadResponse(
                success = true,
                message = "File loaded successfully",
                successCount = result.successCount,
                errorCount = result.errorCount,
                errors = result.errors
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error loading file $fileName: ${e.message}", e)
            val response = LoadResponse(
                success = false,
                message = "Error loading file: ${e.message}",
                successCount = 0,
                errorCount = 0,
                errors = listOf(e.message ?: "Unknown error")
            )
            ResponseEntity.badRequest().body(response)
        }
    }

    @PostMapping("/test")
    @ResponseBody
    fun testDatabaseInsertion(): ResponseEntity<String> {
        return try {
            val savedTrack = dataLoadingService.testDatabaseInsertion()
            logger.info("Test track saved with ID: ${savedTrack.id}")
            ResponseEntity.ok("Test track saved successfully with ID: ${savedTrack.id}")
        } catch (e: Exception) {
            logger.error("Test insertion failed: ${e.message}", e)
            ResponseEntity.badRequest().body("Test insertion failed: ${e.message}")
        }
    }

    data class LoadResponse(
        val success: Boolean,
        val message: String,
        val successCount: Int,
        val errorCount: Int,
        val errors: List<String>
    )
}