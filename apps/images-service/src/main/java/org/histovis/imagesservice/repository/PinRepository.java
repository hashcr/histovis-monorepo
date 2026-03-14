package org.histovis.imagesservice.repository;

import org.histovis.imagesservice.model.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PinRepository extends JpaRepository<Pin, UUID> {

    @Query("SELECT p FROM Pin p WHERE p.image.id = :imageId " +
           "AND (p.isPublic = true OR p.ownerEmail = :ownerEmail)")
    List<Pin> findVisiblePins(@Param("imageId") UUID imageId, @Param("ownerEmail") String ownerEmail);

    List<Pin> findByImageId(UUID imageId);
}
