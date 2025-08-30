// repo/UserAccountRepository.java
package org.cheetah.pulse.repo;

import org.cheetah.pulse.domain.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {
  Optional<UserAccount> findByPhone(String phone);
  boolean existsByPhone(String phone);
}