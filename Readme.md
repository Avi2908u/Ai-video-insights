# AI Video Insights Platform

An AI-powered video analysis platform that analyzes YouTube videos and generates an AI-curated summary, key topics, timestamped study notes, and transcript-grounded question answering (RAG).

---

## Architecture Overview

```text
Frontend (Browser / Static Workspace)
  │
  ▼  HTTP (Port 8080)
Spring Boot Backend API
  ├──> YouTube Data API v3 (Fetch title, channel, duration metadata)
  ├──> FastAPI Transcript & RAG Service (Port 8000)
  │      ├──> YouTube Transcript Extraction (en, hi)
  │      └──> LangChain RAG & Chroma Vector Store
  ├──> OpenRouter API (LLM Summary, Key Topics, Timestamped Notes)
  └──> PostgreSQL Database (Port 5432) (Persistence of video, transcript, & AI insights)
```

---

## Configuration (`.env`)

The project uses a **single root `.env` file** at the project root (`AI-video-insights/.env`) as the single source of truth for both Spring Boot and the Python transcript service.

Create or update `.env` in the root directory:

```env
# Java 21 SDK Path (automatically read by Maven wrapper)
JAVA_HOME=C:\Program Files\Java\jdk-21.0.11

# PostgreSQL Database Connection
DB_URL=jdbc:postgresql://localhost:5432/ai_video_insights
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password

# External AI & YouTube APIs
YOUTUBE_API_KEY=your_youtube_data_api_v3_key
OPENROUTER_API_KEY=your_openrouter_api_key

# Internal Service URL
TRANSCRIPT_SERVICE_URL=http://localhost:8000
```

> [!NOTE]
> **Automatic `.env` Resolution in Spring Boot**:
> When Spring Boot starts, `BackendApplication` automatically scans and loads the root `.env` into Java System Properties (`spring.datasource.*`, `youtube.api.key`, `openrouter.api.key`). You do not need to manually export environment variables in your terminal.

---

## Prerequisites

Before running the project, make sure the following are installed:
- **Java 21** (JDK 21 LTS)
- **Python 3.10+**
- **PostgreSQL 17** (or compatible 14+)
- **Maven** (included via wrapper `./mvnw` or `mvnw.cmd`)

---

## How to Run the Project (Step-by-Step)

Run each service in a separate terminal window:

### Step 1: Start PostgreSQL and Create the Database

1. Ensure your PostgreSQL service is running:
   - **Windows (Command Prompt / PowerShell as Administrator)**:
     ```cmd
     net start postgresql-x64-17
     ```
   - **Alternative**: Open Windows Services (`services.msc`), find `postgresql-x64-17`, and click **Start**.
   - **Linux / macOS**:
     ```bash
     sudo systemctl start postgresql
     ```

2. Create the database configured in your `.env` (if not already created):
   - Using PostgreSQL CLI:
     ```bash
     createdb -U postgres -h localhost -p 5432 ai_video_insights
     ```
   - Or run in `psql`:
     ```sql
     CREATE DATABASE ai_video_insights;
     ```

---

### Step 2: Start the FastAPI Transcript Service (Port 8000)

1. Navigate to the `transcript-service` folder:
   ```bash
   cd transcript-service
   ```

2. Activate the Python virtual environment:
   - **Windows PowerShell**:
     ```powershell
     .\venv\Scripts\Activate.ps1
     ```
   - **Windows CMD**:
     ```cmd
     .\venv\Scripts\activate.bat
     ```
   - **Linux / macOS**:
     ```bash
     source venv/bin/activate
     ```

3. (Optional) If running for the first time or using RAG vector Q&A, install requirements:
   ```bash
   pip install -r requirements.txt
   ```

4. Start the service with Uvicorn:
   ```bash
   uvicorn app:app --host 0.0.0.0 --port 8000
   ```
   Interactive API documentation will be accessible at: `http://localhost:8000/docs`

---

### Step 3: Start the Spring Boot Backend (Port 8080)

1. Open a new terminal and navigate to `backend`:
   ```bash
   cd backend
   ```

2. Run the application via the Maven wrapper:
   - **Windows (PowerShell or CMD)**:
     ```cmd
     .\mvnw.cmd spring-boot:run
     ```
   - **Linux / macOS**:
     ```bash
     ./mvnw spring-boot:run
     ```

   > [!TIP]
   > The Maven wrapper (`mvnw.cmd` / `mvnw`) automatically reads `JAVA_HOME` directly from `.env`, so you do not need to manually set or export `JAVA_HOME` in every terminal session!
   > If you need to manually override `JAVA_HOME`, you can run `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"` (PowerShell) or `set JAVA_HOME=...` (CMD).
   The backend will automatically load `.env`, connect to PostgreSQL, create/update database tables via Hibernate, and listen on `http://localhost:8080`.

---

### Step 4: Start the Frontend Learning Workspace (Port 5500)

1. Open a new terminal at the project root:
   ```bash
   cd "AI-video-insights"
   ```

2. Serve the static frontend using Python's built-in HTTP server:
   ```bash
   python -m http.server 5500 --directory frontend
   ```

3. Open your browser and navigate to:
   **`http://localhost:5500`**

---

## Testing the Platform End-to-End

1. Open `http://localhost:5500` in your web browser.
2. Click **Analyze Video** in the sidebar.
3. Paste any YouTube video link that contains captions/transcript (e.g., `https://www.youtube.com/watch?v=dQw4w9WgXcQ` or an educational lecture).
4. Click **Analyze Video**.
5. The platform will:
   - Extract title, duration, and channel metadata via YouTube Data API.
   - Fetch the timestamped transcript segments via the Python service.
   - Generate a concise summary and key topic breakdown via OpenRouter.
   - Generate timestamped study notes and persist the record in PostgreSQL.
   - Display the results with interactive timestamp links to YouTube and enable transcript-grounded Q&A.

---

## API Endpoints Reference

| Method | Endpoint | Service | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/video/analyze?url={youtubeUrl}` | Spring Boot (`:8080`) | Analyzes a video (fetches metadata, transcript, generates summary/notes, persists in DB). |
| `POST` | `/api/video/{videoId}/ask` | Spring Boot (`:8080`) | Grounded question answering over the video transcript with citations. |
| `GET` | `/api/transcript/{videoId}` | Spring Boot (`:8080`) | Fetches cached or new transcript for a video. |
| `GET` | `/transcript/{video_id}` | FastAPI (`:8000`) | Extracts English/Hindi transcript segments from YouTube. |
| `POST` | `/ask` | FastAPI (`:8000`) | Chroma vector similarity search and RAG answering via OpenRouter. |

---

## Troubleshooting

- **`SQL Error: 0, SQLState: 08001 / Connection attempt failed`**:
  Make sure your PostgreSQL service is running (`net start postgresql-x64-17`) and the database `ai_video_insights` has been created.
- **`Unsupported class file major version / Java version mismatch`**:
  Ensure you are building with Java 21 (`$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"`).
- **`OPENROUTER_API_KEY is not configured`**:
  Verify that `OPENROUTER_API_KEY` is present in the root `.env` file without syntax errors.
