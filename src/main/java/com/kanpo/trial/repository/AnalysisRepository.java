package com.kanpo.trial.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanpo.trial.model.Analysis;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long>{
	public Optional<Analysis> findById(Long id);
}
