package com.Aivideoinsights.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Summary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Video video;
    private String summaryType;

    @Column(columnDefinition = "TEXT")
    private String summaryText;
}
