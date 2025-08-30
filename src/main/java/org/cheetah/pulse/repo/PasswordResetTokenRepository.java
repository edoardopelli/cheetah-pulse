// src/main/java/com/setupnet/johndoe/repo/PasswordResetTokenRepository.java
package org.cheetah.pulse.repo;

import org.cheetah.pulse.domain.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
  Optional<PasswordResetToken> findTopByPhoneOrderByExpiresAtDesc(String phone);
  void deleteByPhone(String phone);
}