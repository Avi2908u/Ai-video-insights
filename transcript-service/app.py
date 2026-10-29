from fastapi import FastAPI, HTTPException
from youtube_transcript_api import YouTubeTranscriptApi

app = FastAPI(
    title="AI Video Insights Transcript Service",
    description="Fetches YouTube transcripts for the Spring Boot backend.",
    version="1.0.0",
)


@app.get("/transcript/{video_id}")
def get_transcript(video_id: str) -> dict:
    try:
        transcript = YouTubeTranscriptApi().fetch(video_id, languages=["en", "hi"])
        segments = [
            {"text": snippet.text, "start": snippet.start}
            for snippet in transcript
        ]
        return {
            "videoId": video_id,
            "language": getattr(transcript, "language", "unknown"),
            "transcript": " ".join(segment["text"] for segment in segments),
            "segments": segments,
        }
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail={"videoId": video_id, "error": str(exc)},
        ) from exc
