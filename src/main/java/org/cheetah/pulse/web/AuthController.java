// web/AuthController.java
package org.cheetah.pulse.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.cheetah.pulse.dto.*;
import org.cheetah.pulse.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
    authService.register(req);
    return ResponseEntity.ok(new AuthResponse("OTP inviato via SMS", null));
  }

  @PostMapping("/resend")
  public ResponseEntity<AuthResponse> resend(@RequestParam String phone) {
    authService.resendOtp(phone);
    return ResponseEntity.ok(new AuthResponse("Nuovo OTP inviato", null));
  }

  @PostMapping("/verify")
  public ResponseEntity<AuthResponse> verify(@Valid @RequestBody VerifyOtpRequest req) {
    authService.verify(req.getPhone(), req.getCode());
    return ResponseEntity.ok(new AuthResponse("Registrazione confermata", null));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
    String token = authService.login(req);
    return ResponseEntity.ok(new AuthResponse("Login ok", token));
  }
}