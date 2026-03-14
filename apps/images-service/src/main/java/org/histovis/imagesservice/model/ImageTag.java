package org.histovis.imagesservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "image_tags", indexes = {
        @Index(name = "idx_image_tags_image_id", columnList = "image_id"),
        @Index(name = "idx_image_tags_tag", columnList = "tag")
})
@Getter
@Setter
@NoArgsConstructor
public class ImageTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "tag", nullable = false)
    private String tag;

    public ImageTag(Image image, String tag) {
        this.image = image;
        this.tag = tag;
    }
}
