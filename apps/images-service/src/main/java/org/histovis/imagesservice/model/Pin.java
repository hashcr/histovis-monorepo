package org.histovis.imagesservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pins", indexes = {
        @Index(name = "idx_pins_image_id", columnList = "image_id"),
        @Index(name = "idx_pins_owner_email", columnList = "owner_email")
})
@Getter
@Setter
@NoArgsConstructor
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "owner_email")
    private String ownerEmail;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "x", nullable = false)
    private Double x;

    @Column(name = "y", nullable = false)
    private Double y;

    @Column(name = "zoom")
    private Double zoom;

    @Column(name = "sequence_id")
    private Integer sequenceId;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
