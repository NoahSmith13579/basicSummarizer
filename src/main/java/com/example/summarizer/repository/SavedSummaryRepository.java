package com.example.summarizer.repository;

import com.example.summarizer.model.SavedSummary;
import com.example.summarizer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSummaryRepository extends JpaRepository<SavedSummary, Long> {
    List<SavedSummary> findByUser(User user);
    List<SavedSummary> findByUserOrderByCreatedAtDesc(User user);
    Optional<SavedSummary> findByIdAndUser(Long id, User user);
    List<SavedSummary> findByUserAndSourceType(User user, String sourceType);
    long countByUser(User user);
}