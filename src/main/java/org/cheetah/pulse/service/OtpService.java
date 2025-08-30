// service/OtpService.java
package org.cheetah.pulse.service;

import lombok.RequiredArgsConstructor;

import org.cheetah.pulse.domain.OtpToken;
import org.cheetah.pulse.repo.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class OtpService {
  private final OtpTokenRepository otpRepo;
  private static final SecureRandom RNG = new SecureRandom();

  @Value("${app.otp.length:6}") private int otpLength;
  @Value("${app.otp.ttl-seconds:300}") private long ttlSeconds;
  @Value("${app.otp.max-attempts:5}") private int maxAttempts;

  public String generateCode() {
    int max = (int)Math.pow(10, otpLength);
    int min = max/10;
    int code = RNG.nextInt(max - min) + min;
    return String.format("%0" + otpLength + "d", code);
  }

  public OtpToken issue(String phone) {
    otpRepo.deleteByPhone(phone);
    var token = OtpToken.newToken(phone, generateCode(), ttlSeconds);
    return otpRepo.save(token);
  }

  public boolean verify(String phone, String code) {
    var opt = otpRepo.findTopByPhoneOrderByExpiresAtDesc(phone);
    if (opt.isEmpty()) return false;
    var token = opt.get();
    if (token.getAttempts() >= maxAttempts) return false;

    boolean ok = token.getCode().equals(code);
    token.setAttempts(token.getAttempts()+1);
    otpRepo.save(token);
    if (ok) otpRepo.deleteByPhone(phone);
    return ok;
  }
}