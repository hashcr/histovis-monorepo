package org.histovis.imagesservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_image_id", columnList = "image_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "email")
    private String email;

    @Column(name = "author")
    private String author;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    public Comment(Image image, String email, String author, String text) {
        this.image = image;
        this.email = email;
        this.author = author;
        this.text = text;
    }
}
