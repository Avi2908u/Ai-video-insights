package com.Aivideoinsights.backend.repository;

import com.Aivideoinsights.backend.model.AiResults;
import com.Aivideoinsights.backend.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiResultsRepository extends JpaRepository<AiResults, Long> {
    Optional<AiResults> findByVideo(Video video);
}
