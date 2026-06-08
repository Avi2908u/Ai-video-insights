# AI Video Insights Platform

## Overview

AI Video Insights Platform is an AI-powered learning assistant that analyzes educational and research videos to generate summaries, notes, key topics, prerequisites, and intelligent Q&A.

The goal is to help users learn faster from long-form video content without manually taking notes or searching through hours of footage.

---

## Features

### Current

* YouTube video analysis
* Transcript extraction
* AI-generated summaries
* Key topic extraction
* Structured notes generation

### Planned

* Prerequisite detection
* Timestamped highlights
* RAG-based Q&A
* Learning roadmaps
* Flashcards and quizzes
* User authentication
* Multi-video knowledge base

---

## Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security
* JWT
* PostgreSQL

### AI Layer

* Python
* FastAPI
* OpenAI API
* LangChain
* ChromaDB

### DevOps

* Git & GitHub
* Docker

---

## Architecture

```text
User
  ↓
Spring Boot Backend
  ↓
Python AI Service
  ↓
LLM + RAG Pipeline
  ↓
PostgreSQL / Vector DB
```

---

## Roadmap

### Phase 1

* [x] Transcript extraction
* [x] Video summarization
* [x] Store analysis results

### Phase 2

* [x] Topic extraction
* [x] Notes generation
* [ ] Timestamped insights

### Phase 3

* [ ] RAG-based Q&A
* [ ] Vector database integration

### Phase 4

* [ ] JWT Authentication
* [ ] User dashboard
* [ ] Docker deployment

---

## Learning Goals

This project is being built to gain hands-on experience with:

* Spring Boot
* REST APIs
* Database Design
* LLMs & RAG
* Vector Databases
* Docker
* Scalable Backend Development

---

## Status

🚧 Currently in development.
