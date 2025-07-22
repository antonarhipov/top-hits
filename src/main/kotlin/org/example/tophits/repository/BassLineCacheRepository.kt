package org.example.tophits.repository

import org.example.tophits.model.BassLineCache
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BassLineCacheRepository : CrudRepository<BassLineCache, Long> {
    
    @Query("SELECT * FROM bass_line_cache WHERE artist_name = :artistName AND track_name = :trackName")
    fun findByArtistNameAndTrackName(@Param("artistName") artistName: String, @Param("trackName") trackName: String): Optional<BassLineCache>
    
    @Query("DELETE FROM bass_line_cache WHERE artist_name = :artistName AND track_name = :trackName")
    fun deleteByArtistNameAndTrackName(@Param("artistName") artistName: String, @Param("trackName") trackName: String)
}