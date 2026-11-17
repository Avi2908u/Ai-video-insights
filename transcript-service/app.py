from __future__ import annotations

import hashlib
import os
from pathlib import Path

import requests
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from youtube_transcript_api import YouTubeTranscriptApi

# Load environment variables from the project root .env (override=True ensures .env values take precedence)
root_env = Path(__file__).resolve().parent.parent / ".env"
if root_env.exists():
    load_dotenv(root_env, override=True)
else:
    load_dotenv(override=True)

try:
    from langchain_core.documents import Document
    from langchain_community.vectorstores import Chroma
    from langchain_huggingface import HuggingFaceEmbeddings
    from langchain_text_splitters import RecursiveCharacterTextSplitter
    RAG_AVAILABLE = True
except ImportError:
    Document = None  # type: ignore
    Chroma = None  # type: ignore
    HuggingFaceEmbeddings = None  # type: ignore
    RecursiveCharacterTextSplitter = None  # type: ignore
    RAG_AVAILABLE = False

app = FastAPI(
    title="AI Video Insights Transcript Service",
    description="Fetches YouTube transcripts for the Spring Boot backend.",
    version="1.0.0",
)

class AskRequest(BaseModel):
    videoId: str
    question: str = Field(min_length=2, max_length=1000)
    segments: list[dict] = Field(default_factory=list)
    transcript: str = ""


class Citation(BaseModel):
    timestamp: float | None = None
    text: str


class AskResponse(BaseModel):
    answer: str
    citations: list[Citation]


def transcript_documents(payload: AskRequest) -> list[Document]:
    """Pack nearby transcript segments into ~450-token chunks with ~70-token overlap."""
    segments = payload.segments
    if not segments and payload.transcript:
        segments = [{"text": payload.transcript, "start": 0}]
    if not segments:
        raise HTTPException(status_code=422, detail="No transcript is available for this video.")

    splitter = RecursiveCharacterTextSplitter(chunk_size=1800, chunk_overlap=280, add_start_index=False)
    docs: list[Document] = []
    current: list[dict] = []
    size = 0
    for segment in segments:
        text = str(segment.get("text", "")).strip()
        if not text:
            continue
        if current and size + len(text) > 1800:
            raw = " ".join(item["text"] for item in current)
            start = current[0].get("start", 0)
            end = current[-1].get("start", start)
            for part in splitter.split_text(raw):
                docs.append(Document(page_content=part, metadata={"start": start, "end": end}))
            # Keep complete recent utterances so a question can use nearby context.
            overlap, overlap_size = [], 0
            for item in reversed(current):
                overlap.insert(0, item)
                overlap_size += len(item["text"])
                if overlap_size >= 280:
                    break
            current, size = overlap, overlap_size
        current.append({"text": text, "start": segment.get("start", 0)})
        size += len(text)
    if current:
        raw = " ".join(item["text"] for item in current)
        start = current[0].get("start", 0)
        end = current[-1].get("start", start)
        docs.extend(Document(page_content=part, metadata={"start": start, "end": end}) for part in splitter.split_text(raw))
    return docs


@app.post("/ask", response_model=AskResponse)
def ask_video(payload: AskRequest) -> AskResponse:
    if not RAG_AVAILABLE:
        raise HTTPException(
            status_code=503,
            detail="RAG dependencies (langchain, chromadb, sentence-transformers) are not installed in the virtual environment. Please run: pip install -r requirements.txt",
        )
    api_key = os.getenv("OPENROUTER_API_KEY")
    if not api_key:
        raise HTTPException(status_code=503, detail="OPENROUTER_API_KEY is not configured.")
    docs = transcript_documents(payload)
    fingerprint = hashlib.sha256((payload.videoId + "\n" + "\n".join(d.page_content for d in docs)).encode()).hexdigest()
    index_dir = Path(os.getenv("RAG_INDEX_DIR", "./rag-index")) / fingerprint
    try:
        embeddings = HuggingFaceEmbeddings(model_name=os.getenv("RAG_EMBEDDING_MODEL", "sentence-transformers/all-MiniLM-L6-v2"))
        if (index_dir / "chroma.sqlite3").exists():
            store = Chroma(persist_directory=str(index_dir), embedding_function=embeddings)
        else:
            index_dir.parent.mkdir(parents=True, exist_ok=True)
            store = Chroma.from_documents(docs, embeddings, persist_directory=str(index_dir))
        matches = store.similarity_search(payload.question, k=min(5, len(docs)))
        context = "\n\n".join(f"[{int(float(doc.metadata.get('start', 0)))}s] {doc.page_content}" for doc in matches)
        response = requests.post(
            os.getenv("OPENROUTER_URL", "https://openrouter.ai/api/v1/chat/completions"),
            headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json", "X-Title": "AI Video Insights"},
            json={"model": os.getenv("OPENROUTER_MODEL", "openai/gpt-4o-mini"), "messages": [
                {"role": "system", "content": "Answer only from the supplied video transcript context. If it does not contain the answer, say so. Keep the answer clear and concise. Do not invent timestamps; citations are returned separately."},
                {"role": "user", "content": f"Question: {payload.question}\n\nTranscript context:\n{context}"}], "temperature": 0.2}, timeout=60)
        response.raise_for_status()
        answer = response.json()["choices"][0]["message"]["content"]
        citations = []
        seen = set()
        for doc in matches:
            start = float(doc.metadata.get("start", 0))
            key = (start, doc.page_content[:100])
            if key not in seen:
                seen.add(key)
                citations.append(Citation(timestamp=start, text=doc.page_content[:280]))
        return AskResponse(answer=answer, citations=citations)
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"Question answering failed: {exc}") from exc


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
