package com.kanpo.trial.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanpo.trial.model.Medicine;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long>{
	public Optional<Medicine> findById(Long id);
}
