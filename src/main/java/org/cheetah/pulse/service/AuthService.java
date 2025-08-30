// service/AuthService.java
package org.cheetah.pulse.service;

import lombok.RequiredArgsConstructor;

import org.cheetah.pulse.domain.UserAccount;
import org.cheetah.pulse.dto.LoginRequest;
import org.cheetah.pulse.dto.RegisterRequest;
import org.cheetah.pulse.repo.UserAccountRepository;
import org.cheetah.pulse.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserAccountRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final PhoneNormalizer phoneNormalizer;
  private final OtpService otpService;
  private final SmsSender smsSender;
  private final JwtService jwtService;

  public void register(RegisterRequest req) {
    if (!req.getPassword().equals(req.getConfirmPassword())) {
      throw new IllegalArgumentException("Password e conferma non coincidono.");
    }
    String phone = phoneNormalizer.normalize(req.getPhone());
    if (userRepo.existsByPhone(phone)) throw new IllegalArgumentException("Telefono già registrato.");

    var user = UserAccount.builder()
        .phone(phone)
        .passwordHash(passwordEncoder.encode(req.getPassword()))
        .enabled(false)
        .createdAt(Instant.now())
        .build();
    userRepo.save(user);

    var token = otpService.issue(phone);
    smsSender.send(phone, "Il tuo codice di verifica è: " + token.getCode());
  }

  public void resendOtp(String phoneRaw) {
    String phone = phoneNormalizer.normalize(phoneRaw);
    var user = userRepo.findByPhone(phone)
        .orElseThrow(() -> new IllegalArgumentException("Utente non trovato."));
    if (user.isEnabled()) throw new IllegalStateException("Utente già verificato.");
    var token = otpService.issue(phone);
    smsSender.send(phone, "Il tuo codice di verifica è: " + token.getCode());
  }

  public void verify(String phoneRaw, String code) {
    String phone = phoneNormalizer.normalize(phoneRaw);
    boolean ok = otpService.verify(phone, code);
    if (!ok) throw new IllegalArgumentException("OTP non valido o scaduto.");

    var user = userRepo.findByPhone(phone).orElseThrow();
    user.setEnabled(true);
    user.setVerifiedAt(Instant.now());
    userRepo.save(user);
  }

  /** Ritorna un JWT firmato con subject = userId */
  public String login(LoginRequest req) {
    String phone = phoneNormalizer.normalize(req.getPhone());
    var user = userRepo.findByPhone(phone)
        .orElseThrow(() -> new IllegalArgumentException("Credenziali non valide."));
    if (!user.isEnabled()) throw new IllegalStateException("Account non verificato.");
    if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Credenziali non valide.");
    }
    return jwtService.generate(user.getId());
  }
}