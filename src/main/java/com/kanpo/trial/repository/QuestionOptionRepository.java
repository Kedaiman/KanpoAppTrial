package com.kanpo.trial.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanpo.trial.model.QuestionOption;

/**
 * QuestionOptionリポジトリインターフェイス
 * @author keita
 */
@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long>{
	public Optional<QuestionOption> findById(Long id);
}
