package com.skillgap.repository;

import com.skillgap.entity.AnalysisResult;
import com.skillgap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    List<AnalysisResult> findByUserOrderByAnalyzedAtDesc(User user);
    long countByUser(User user);
}