# AI Video Insights Platform

An in-development application that analyzes YouTube videos and returns the transcript, an AI-generated summary, key topics, and timestamped study notes.

## Current architecture

```text
Client
  -> Spring Boot API (video metadata, AI prompts, persistence)
       -> FastAPI transcript service (YouTube transcript extraction)
       -> OpenRouter (summary, topics, notes)
       -> PostgreSQL (video, transcript, analysis results)
```

Spring Boot is the application backend. The Python service currently handles transcript extraction only; AI generation is performed by Spring Boot through OpenRouter. There is no RAG pipeline or vector database yet.

## Implemented functionality

- Analyze a YouTube video through `GET /api/video/analyze?url=<youtube-url>`.
- Fetch video title, channel, and duration through the YouTube Data API.
- Fetch English or Hindi transcript segments from the FastAPI service.
- Generate a 200-400 word summary and key topics through OpenRouter.
- Generate timestamped study notes from transcript segments.
- Persist video metadata, transcript text, summary, topics, and notes in PostgreSQL.
- Return previously stored results when the video has already been analyzed.
- Fetch a transcript through `GET /api/transcript/{videoId}` on Spring Boot.

## Services

### Frontend (`frontend`)

The frontend is a static HTML, CSS, and JavaScript workspace modeled on the screenshots in `demo`. It connects to the Spring Boot API at `http://localhost:8080` by default. Serve the folder with any static web server, for example from the repository root:

```bash
python -m http.server 5500 --directory frontend
```

Then open `http://localhost:5500`. To use a different API host, set `window.VIDEO_INSIGHTS_API_URL` before `app.js` loads, or set the browser local storage key `avi-api-base`. Analyzed results are retained in that browser's local storage; the backend remains the source for new analysis and persists its own records in PostgreSQL.

The workspace includes an overview with metrics from saved analyses, video analysis, a searchable library, per-video summary/topics/notes/transcript views, timestamp links to YouTube, and study notes. Roadmaps and quizzes are shown as unavailable because the backend does not implement those features yet.

### Spring Boot (`backend`)

Requires Java 21 and Maven. Run from `backend` with `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). Configure the variables listed below before starting.

### FastAPI transcript service (`transcript-service`)

Requires Python 3.10 or newer. From this directory, create/activate a virtual environment, install `requirements.txt`, then run:

```bash
uvicorn app:app --host 0.0.0.0 --port 8000
```

Interactive API docs are available at `/docs` while the service is running.

## Configuration

Set these environment variables for Spring Boot:

| Variable | Purpose |
| --- | --- |
| `YOUTUBE_API_KEY` | YouTube Data API key for video metadata |
| `OPENROUTER_API_KEY` | OpenRouter API key for generated content |
| `DB_URL` | JDBC PostgreSQL URL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `TRANSCRIPT_SERVICE_URL` | Optional transcript service base URL; defaults to `http://localhost:8000` |

Provide these values as environment variables to the Spring Boot process. Spring Boot does not load the root `.env` file automatically. Do not commit real credentials; an `.env.example` can document variable names with placeholders.

## Not implemented yet

- Authentication and authorization (Spring Security/JWT are not currently configured).
- RAG-based question answering and vector database integration.
- Learning roadmaps, flashcards, quizzes, multi-video knowledge search, and dashboard.
- Docker deployment and a job queue for long-running analysis.

## Deployment status

The services can be deployed separately to hosts that provide Java 21, Python, and PostgreSQL, with network access between Spring Boot and the transcript service. This repository does not yet include Dockerfiles or a Compose setup, automated database migrations, or production authentication and request protection, so deployment is currently manual and is not production-hardened.

## Repository layout

```text
frontend/             Static learning workspace
backend/              Spring Boot API and persistence
transcript-service/   FastAPI YouTube transcript service
```
