package org.histovis.analysisservice.repository;

import org.histovis.analysisservice.model.Plugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PluginRepository extends JpaRepository<Plugin, UUID> {

    Optional<Plugin> findByCode(String code);
}
