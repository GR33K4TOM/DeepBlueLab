package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.RescueCenter;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RescueCenterRepository extends JpaRepository<RescueCenter, Long> {

	Optional<RescueCenter> findByCode(String code);
}
