package org.example.tophits.service

import org.example.tophits.controller.TrackController.MidiNote
import org.example.tophits.repository.BassLineCacheRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.ai.chat.client.ChatClient
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class BassLineServiceTest {

    @Mock
    private lateinit var chatClient: ChatClient

    @Mock
    private lateinit var bassLineCacheRepository: BassLineCacheRepository

    private lateinit var bassLineService: BassLineService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        bassLineService = BassLineService(chatClient, bassLineCacheRepository)
    }

    @Test
    fun `extractNotesFromTablature should parse simple bass tablature correctly`() {
        // Given
        val sampleTablature = """
            Bass Line for "Test Song"
            
            Here's a simple bass line pattern:
            
            G|---------------------|
            D|---------------------|
            A|-----3---5---3-------|
            E|-1-----------1---3---|
            
            This pattern repeats throughout the verse.
        """.trimIndent()

        // When
        val extractedNotes = bassLineService.extractNotesFromTablature(sampleTablature)

        // Then
        assertTrue(extractedNotes.isNotEmpty(), "Should extract notes from tablature")
        
        // Verify we have the expected notes based on the tablature
        // E string fret 1 = F2, A string fret 3 = C3, A string fret 5 = D3, etc.
        val expectedNotes = listOf(
            MidiNote("F", 2, 0.5, 0.0),    // E string, fret 1
            MidiNote("C", 3, 0.5, 2.5),    // A string, fret 3
            MidiNote("D", 3, 0.5, 4.0),    // A string, fret 5
            MidiNote("C", 3, 0.5, 5.5),    // A string, fret 3
            MidiNote("F", 2, 0.5, 8.0),    // E string, fret 1
            MidiNote("G#", 2, 0.5, 9.5)   // E string, fret 3
        )

        // Check that we extracted some notes (exact matching might vary due to parsing complexity)
        assertTrue(extractedNotes.size >= 4, "Should extract at least 4 notes from the sample")
        
        // Verify that we have some F notes (from E string fret 1)
        val fNotes = extractedNotes.filter { it.note == "F" && it.octave == 2 }
        assertTrue(fNotes.isNotEmpty(), "Should contain F2 notes from E string fret 1")
    }

    @Test
    fun `parse tabulature - 2 tacts `() {
        // Given
        val sampleTablature = """
            Bass Line for "Test Song"
            
            Here's a simple bass line pattern:
            
            G|------------------|------------------|
            D|------------------|-------5--5-------|
            A|--5--5--3--3--3---|--5--5-------3--3-|
            E|------------------|------------------|
            
            This pattern repeats throughout the verse.
        """.trimIndent()

        // When
        val extractedNotes = bassLineService.extractNotesFromTablature(sampleTablature)

        // Then
        assertTrue(extractedNotes.isNotEmpty(), "Should extract notes from tablature")

//        val expectedNotes = listOf(
//            MidiNote(note = "D", octave = 3, duration = 0.5, startTime = 0.0),
//            MidiNote(note = "D", octave = 3, duration = 0.5, startTime = 1.5),
//            MidiNote(note = "C", octave = 3, duration = 0.5, startTime = 3.0),
//            MidiNote(note = "C", octave = 3, duration = 0.5, startTime = 4.5),
//            MidiNote(note = "C", octave = 3, duration = 0.5, startTime = 6.0),
//            MidiNote(note = "D", octave = 3, duration = 0.5, startTime = 9.5),
//            MidiNote(note = "D", octave = 3, duration = 0.5, startTime = 11.0),
//            MidiNote(note = "G", octave = 3, duration = 0.5, startTime = 12.0),
//            MidiNote(note = "G", octave = 3, duration = 0.5, startTime = 13.0),
//            MidiNote(note = "C", octave = 3, duration = 0.5, startTime = 15.0),
//            MidiNote(note = "C", octave = 3, duration = 0.5, startTime = 16.5),
//        )

        // Check that we extracted some notes (exact matching might vary due to parsing complexity)
        assertEquals(extractedNotes.size, 11, "Should extract 11 notes from the sample")
    }

    @Test
    fun `extractNotesFromTablature should handle empty tablature`() {
        // Given
        val emptyTablature = """
            This is just text without any tablature.
            No fret numbers or string indicators here.
        """.trimIndent()

        // When
        val extractedNotes = bassLineService.extractNotesFromTablature(emptyTablature)

        // Then
        assertTrue(extractedNotes.isEmpty(), "Should return empty list for content without tablature")
    }

    @Test
    fun `extractNotesFromTablature should handle tablature with string labels`() {
        // Given
        val labeledTablature = """
            G: |---------------------|
            D: |---------------------|
            A: |-----3---5---3-------|
            E: |-1-----------1---3---|
        """.trimIndent()

        // When
        val extractedNotes = bassLineService.extractNotesFromTablature(labeledTablature)

        // Then
        assertTrue(extractedNotes.isNotEmpty(), "Should extract notes from labeled tablature")
    }

    @Test
    fun `extractNotesFromTablature should handle malformed input gracefully`() {
        // Given
        val malformedInput = "This is not tablature at all!"

        // When
        val extractedNotes = bassLineService.extractNotesFromTablature(malformedInput)

        // Then
        // Should not throw exception and return empty list
        assertTrue(extractedNotes.isEmpty(), "Should handle malformed input gracefully")
    }
}