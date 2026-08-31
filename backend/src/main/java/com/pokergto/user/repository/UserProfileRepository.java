package com.pokergto.user.repository;

import com.pokergto.user.model.UserProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {}
