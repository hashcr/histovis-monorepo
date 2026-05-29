package org.histovis.imagesservice.repository;

import org.histovis.imagesservice.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    @Query("SELECT i FROM Image i WHERE " +
           "i.viewableImageUrl IS NOT NULL AND " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(i.fileName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Image> searchByText(@Param("query") String query);

    // Tags and comments are loaded lazily within the calling @Transactional service method.
    // Using two separate JOIN FETCHes on List collections causes MultipleBagFetchException.
    java.util.Optional<Image> findById(UUID id);

    default java.util.Optional<Image> findByIdWithDetails(UUID id) {
        return findById(id);
    }
}
