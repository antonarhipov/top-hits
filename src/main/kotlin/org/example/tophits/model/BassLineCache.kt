package org.example.tophits.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("bass_line_cache")
data class BassLineCache(
    @Id
    val id: Long? = null,
    @Column("artist_name")
    val artistName: String,
    @Column("track_name")
    val trackName: String,
    @Column("bass_line_content")
    val bassLineContent: String,
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)