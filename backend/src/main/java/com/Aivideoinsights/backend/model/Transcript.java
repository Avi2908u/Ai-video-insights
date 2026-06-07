package com.Aivideoinsights.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="transcripts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transcript {
    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private Video video;

    @Column(columnDefinition = "TEXT")
    private String transcriptText;
}
