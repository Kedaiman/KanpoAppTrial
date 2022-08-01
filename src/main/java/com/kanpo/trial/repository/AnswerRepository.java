package com.kanpo.trial.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanpo.trial.model.Answer;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long>{
	public Optional<Answer> findById(Long id);
}
