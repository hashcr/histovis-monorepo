package org.histovis.imagesservice.repository;

import org.histovis.imagesservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
