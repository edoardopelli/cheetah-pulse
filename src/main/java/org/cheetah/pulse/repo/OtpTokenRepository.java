// repo/OtpTokenRepository.java
package org.cheetah.pulse.repo;

import org.cheetah.pulse.domain.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {
  Optional<OtpToken> findTopByPhoneOrderByExpiresAtDesc(String phone);
  void deleteByPhone(String phone);
}