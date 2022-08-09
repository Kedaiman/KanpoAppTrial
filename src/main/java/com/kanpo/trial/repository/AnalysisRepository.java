package com.kanpo.trial.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanpo.trial.model.Analysis;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long>{
	public Optional<Analysis> findById(Long id);
	public int countByUpdateAtLessThan(Timestamp time);
	public List<Analysis> getByUpdateAtLessThan(Timestamp time);
	//public void deleteAllByUpdateAtLessThan(Timestamp time);
}
