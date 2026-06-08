package com.Aivideoinsights.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String youtubeVideoId;
    private String title;
    private String channel;
    private String duration;
    private LocalDateTime createdAt;
    @Column(columnDefinition = "TEXT")
    private String keyTopics;
    @Column(columnDefinition = "TEXT")
    private String notes;
}
