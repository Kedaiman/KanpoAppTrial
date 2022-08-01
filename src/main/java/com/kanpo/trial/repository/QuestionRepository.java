package com.kanpo.trial.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanpo.trial.model.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>{
	public Optional<Question> findById(Long id);
}
