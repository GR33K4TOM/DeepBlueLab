package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Expertise;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertiseRepository extends JpaRepository<Expertise, Long> {

	Optional<Expertise> findByNameIgnoreCase(String name);
}
