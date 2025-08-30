// src/main/java/com/setupnet/johndoe/service/PasswordChangeService.java
package org.cheetah.pulse.service;

import lombok.RequiredArgsConstructor;

import org.cheetah.pulse.repo.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordChangeService {

  private final UserAccountRepository userRepo;
  private final PasswordEncoder passwordEncoder;

  public void changePassword(String userId, String newPassword, String confirmPassword) {
    if (!newPassword.equals(confirmPassword)) {
      throw new IllegalArgumentException("Password e conferma non coincidono.");
    }
    var user = userRepo.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepo.save(user);
  }
}