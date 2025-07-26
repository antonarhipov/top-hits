# Top Hits - Spotify 2023 Data Analysis & AI Bass Line Generator

A comprehensive Spring Boot application for analyzing Spotify's 2023 top hits data with AI-powered bass line generation capabilities. This project combines data analysis, web APIs, and artificial intelligence to provide insights into popular music trends and generate musical content.

## Features

### 🎵 Music Data Analysis
- **Comprehensive Track Database**: Store and analyze detailed Spotify 2023 track data including streaming numbers, chart positions, and audio features
- **Search & Filtering**: Search tracks by name, artist, or various attributes with pagination and sorting

### 🤖 AI-Powered Music Generation
- **Bass Line Generation**: Generate bass guitar tablature using OpenAI's GPT models based on track characteristics
- **MIDI Note Extraction**: Convert generated bass lines to MIDI format for digital audio workstations
- **Caching**: Cache generated bass lines to optimize performance and reduce API costs
- **Context-Aware Generation**: Uses track attributes (BPM, key signature, mode, audio features) for musically appropriate results

### 📊 Data Processing & Visualization
- **CSV Data Loading**: Import Spotify CSV data through web interface or API
- **Database Management**: PostgreSQL backend with comprehensive data validation

### 🌐 Web Interface & API
- **RESTful API**: Complete REST API for all functionality
- **Web Dashboard**: User-friendly interface for data loading and track browsing
- **Real-time Processing**: Live feedback during data import operations

## Prerequisites

- **Java 21** or higher
- **Docker & Docker Compose** (for database)
- **OpenAI API Key** (for AI features)
- **Gradle** (included via wrapper)

## Project Structure

```
top-hits/
├── src/main/kotlin/org/example/tophits/
│   ├── Application.kt                 # Main Spring Boot application
│   ├── config/                        # Configuration classes
│   │   ├── ChatClientConfig.kt        # OpenAI integration setup
│   │   └── JdbcConfig.kt             # Database configuration
│   ├── controller/                    # REST controllers
│   │   ├── TrackController.kt         # Track management & AI features
│   │   └── DataLoadingController.kt   # CSV data import
│   ├── model/                         # Data models
│   │   ├── Track.kt                   # Main track entity
│   │   └── BassLineCache.kt          # AI result caching
│   ├── repository/                    # Data access layer
│   ├── service/                       # Business logic
│   │   ├── TrackService.kt           # Track operations
│   │   ├── BassLineService.kt        # AI bass line generation
│   │   └── DataLoadingService.kt     # CSV processing
│   └── resources/
│       ├── application.properties     # App configuration
│       ├── schema.sql                # Database schema
│       └── templates/                # Web UI templates
├── data/
│   └── spotify-2023.csv             # Sample Spotify data
├── notebooks/                        # Kotlin Jupyter notebooks
├── compose.yaml                      # Docker Compose configuration
└── build.gradle.kts                 # Build configuration
```

## Building the Project

### 1. Clone the Repository
```bash
git clone [repository-url]
cd top-hits
```

### 2. Set Environment Variables
Create a `.env` file or set environment variables:
```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

### 3. Build the Application
```bash
./gradlew build
```

### 4. Run Tests
```bash
./gradlew test
```

## Running the Project

### Option 1: Using Docker Compose (Recommended)

1. **Start the Database**:
```bash
docker-compose up -d postgres
```

2. **Run the Application**:
```bash
./gradlew bootRun
```

### Option 2: Local Development

1. **Start PostgreSQL** (ensure it's running on localhost:5432)
2. **Create Database**:
```sql
CREATE DATABASE mydatabase;
CREATE USER myuser WITH PASSWORD 'secret';
GRANT ALL PRIVILEGES ON DATABASE mydatabase TO myuser;
```

3. **Run the Application**:
```bash
./gradlew bootRun
```

### Option 3: Using JAR File

1. **Build JAR**:
```bash
./gradlew bootJar
```

2. **Run JAR**:
```bash
java -jar build/libs/top-hits-0.0.1-SNAPSHOT.jar
```

## Configuration

### Database Configuration
The application uses PostgreSQL with the following default settings:
- **Host**: localhost:5432
- **Database**: mydatabase
- **Username**: myuser
- **Password**: secret

### OpenAI Configuration
Set your OpenAI API key as an environment variable:
```bash
export OPENAI_API_KEY=your_api_key_here
```

## API Documentation

### Track Management

#### Get All Tracks
```http
GET /api/tracks?page=0&size=20&sortBy=id&sortDirection=asc&search=&searchType=track
```

**Response**:
```json
{
  "tracks": [
    {
      "id": 1,
      "trackName": "Shape of You",
      "artistName": "Ed Sheeran",
      "releasedYear": 2017,
      "streams": 3562543890
    }
  ],
  "currentPage": 0,
  "totalPages": 10,
  "totalElements": 200,
  "size": 20,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false
}
```

#### Get Track by ID
```http
GET /api/tracks/{id}
```

#### Search Tracks
```http
GET /api/tracks/search?query=shape+of+you&searchType=track&page=0&size=20
```

### AI Features

#### Generate Bass Line
```http
POST /api/tracks/{id}/bass-line
```

**Response**:
```json
{
  "trackId": 1,
  "trackName": "Shape of You",
  "artistName": "Ed Sheeran",
  "bassLineTabs": "G|---2---2---2---2---|\nD|---0---0---0---0---|\nA|-------------------|\nE|-------------------|",
  "error": null
}
```

#### Extract MIDI Notes
```http
POST /api/tracks/{id}/midi-notes
```

**Response**:
```json
{
  "trackId": 1,
  "trackName": "Shape of You",
  "artistName": "Ed Sheeran",
  "midiNotes": [
    {
      "note": "D",
      "octave": 2,
      "duration": 0.5,
      "startTime": 0.0
    }
  ],
  "tempo": 120,
  "error": null
}
```

### Data Loading

#### List Available CSV Files
```http
GET /data/files
```

#### Load CSV File
```http
POST /data/load/{fileName}
```

#### Test Database Connection
```http
POST /data/test
```

## Data Structure

### Track Entity
The `Track` model represents comprehensive Spotify track data:

```kotlin
data class Track(
    val id: Long?,
    val trackName: String,
    val artistName: String,
    val releasedYear: Int,
    val releasedMonth: Int,
    val releasedDay: Int,
    val inSpotifyPlaylists: Int,
    val inSpotifyCharts: Int,
    val streams: Long,
    val inApplePlaylists: Int,
    val inAppleCharts: Int,
    val inDeezerPlaylists: Int,
    val inDeezerCharts: Int,
    val inShazamCharts: Int,
    val bpm: Int?,
    val keySignature: String?,
    val mode: String?,
    val danceabilityPercent: Int?,
    val valencePercent: Int?,
    val energyPercent: Int?,
    val acousticnessPercent: Int?,
    val instrumentalnessPercent: Int?,
    val livenessPercent: Int?,
    val speechinessPercent: Int?
)
```

## Usage Examples

### 1. Loading Data
1. Navigate to `http://localhost:8080/data/load`
2. Select `spotify-2023.csv` from the dropdown
3. Click "Load File" to import the data

### 2. Browsing Tracks
1. Navigate to `http://localhost:8080/tracks`
2. Use search and filter options to explore the data
3. Click on any track to view details

### 3. Generating Bass Lines
```bash
# Get a track ID first
curl "http://localhost:8080/api/tracks?size=1"

# Generate bass line for track ID 1
curl -X POST "http://localhost:8080/api/tracks/1/bass-line"
```

### 4. Using Jupyter Notebooks
1. Install Kotlin Jupyter kernel
2. Open notebooks in the `notebooks/` directory
3. Run cells to explore data and test AI features

## Development

### Adding New Features
1. **Models**: Add new entities in `src/main/kotlin/org/example/tophits/model/`
2. **Services**: Implement business logic in `src/main/kotlin/org/example/tophits/service/`
3. **Controllers**: Add REST endpoints in `src/main/kotlin/org/example/tophits/controller/`
4. **Tests**: Write tests in `src/test/kotlin/`

### Database Migrations
Update `src/main/resources/schema.sql` for schema changes.

### AI Model Configuration
Modify `ChatClientConfig.kt` to adjust OpenAI model settings:
- Model selection (GPT-3.5, GPT-4, etc.)
- Temperature and other parameters
- Token limits and timeouts

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure PostgreSQL is running
   - Check connection parameters in `application.properties`
   - Verify database and user exist

2. **OpenAI API Errors**
   - Verify API key is set correctly
   - Check API quota and billing
   - Ensure network connectivity

3. **CSV Loading Errors**
   - Check file format matches expected schema
   - Verify file permissions
   - Check application logs for detailed errors

4. **Build Failures**
   - Ensure Java 21 is installed
   - Clear Gradle cache: `./gradlew clean`
   - Check for dependency conflicts

### Logs
Application logs are available at:
- Console output during development
- `logs/application.log` (if configured)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- **Spotify** for providing the 2023 music data
- **OpenAI** for GPT models enabling AI bass line generation
- **Spring Boot** and **Spring AI** for the application framework
- **Kotlin** for the programming language