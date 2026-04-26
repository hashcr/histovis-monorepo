package org.histovis.analysisservice.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "plugins")
public class Plugin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String queue;

    @Column(nullable = false)
    private String topic;

    @ElementCollection
    @CollectionTable(name = "plugin_example_args", joinColumns = @JoinColumn(name = "plugin_id"))
    @MapKeyColumn(name = "arg_key")
    @Column(name = "arg_value")
    private Map<String, String> exampleArgs;

    @Column(nullable = false)
    private String installedBy;

    @Column(nullable = false)
    private LocalDateTime installedDate;

    @Column(columnDefinition = "TEXT")
    private String readme;
}
