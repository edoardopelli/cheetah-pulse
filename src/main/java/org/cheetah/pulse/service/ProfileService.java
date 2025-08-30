// src/main/java/com/setupnet/johndoe/service/ProfileService.java
package org.cheetah.pulse.service;

import lombok.RequiredArgsConstructor;

import org.cheetah.pulse.domain.UserAccount;
import org.cheetah.pulse.dto.CompleteProfileRequest;
import org.cheetah.pulse.repo.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final UserAccountRepository userRepo;

  public UserAccount completeMissing(String userId, CompleteProfileRequest req) {
    var user = userRepo.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

    boolean changed = false;

    if (isBlank(user.getName()) && !isBlank(req.getName())) {
      user.setName(req.getName().trim());
      changed = true;
    }
    if (isBlank(user.getSurname()) && !isBlank(req.getSurname())) {
      user.setSurname(req.getSurname().trim());
      changed = true;
    }
    if (user.getBirthDate() == null && req.getBirthDate() != null) {
      user.setBirthDate(req.getBirthDate());
      changed = true;
    }
    if (isBlank(user.getEmail()) && !isBlank(req.getEmail())) {
      user.setEmail(req.getEmail().trim());
      changed = true;
    }

    if (changed) {
      userRepo.save(user);
    }
    return user;
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}