-- Database schema for Spotify 2023 dataset
-- Based on data/spotify-2023.csv structure

CREATE TABLE IF NOT EXISTS tracks (
    id BIGSERIAL PRIMARY KEY,
    track_name VARCHAR(500) NOT NULL,
    artist_name VARCHAR(500) NOT NULL,
    released_year INTEGER NOT NULL,
    released_month INTEGER NOT NULL,
    released_day INTEGER NOT NULL,
    in_spotify_playlists INTEGER NOT NULL DEFAULT 0,
    in_spotify_charts INTEGER NOT NULL DEFAULT 0,
    streams BIGINT NOT NULL DEFAULT 0,
    in_apple_playlists INTEGER NOT NULL DEFAULT 0,
    in_apple_charts INTEGER NOT NULL DEFAULT 0,
    in_deezer_playlists INTEGER NOT NULL DEFAULT 0,
    in_deezer_charts INTEGER NOT NULL DEFAULT 0,
    in_shazam_charts INTEGER NOT NULL DEFAULT 0,
    bpm INTEGER,
    key_signature VARCHAR(10),
    mode VARCHAR(10),
    danceability_percent INTEGER,
    valence_percent INTEGER,
    energy_percent INTEGER,
    acousticness_percent INTEGER,
    instrumentalness_percent INTEGER,
    liveness_percent INTEGER,
    speechiness_percent INTEGER
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_track_name ON tracks (track_name);
CREATE INDEX IF NOT EXISTS idx_artist_name ON tracks (artist_name);

-- Drop existing bass_line_cache table if it exists (for schema migration)
DROP TABLE IF EXISTS bass_line_cache;

-- Bass line cache table for storing generated bass line tabs
CREATE TABLE bass_line_cache (
    id BIGSERIAL PRIMARY KEY,
    artist_name VARCHAR(500) NOT NULL,
    track_name VARCHAR(500) NOT NULL,
    bass_line_content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for efficient cache lookups using artist name + track name
CREATE UNIQUE INDEX idx_bass_line_cache_artist_track ON bass_line_cache (artist_name, track_name);
