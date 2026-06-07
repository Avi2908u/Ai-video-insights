package com.Aivideoinsights.backend.repository;

import com.Aivideoinsights.backend.model.Summary;
import com.Aivideoinsights.backend.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Optional<Summary> findByVideoAndSummaryType(Video video, String summaryType);
}
