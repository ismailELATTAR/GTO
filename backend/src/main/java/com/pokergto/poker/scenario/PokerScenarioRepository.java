package com.pokergto.poker.scenario;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PokerScenarioRepository extends JpaRepository<PokerScenario, UUID> {}
