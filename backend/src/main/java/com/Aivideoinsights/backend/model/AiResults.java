package com.Aivideoinsights.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiResults {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Video video;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String keyTopics;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
