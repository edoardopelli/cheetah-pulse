// src/main/java/com/setupnet/johndoe/web/PasswordResetController.java
package org.cheetah.pulse.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.cheetah.pulse.dto.*;
import org.cheetah.pulse.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

  private final PasswordResetService resetService;

  @PostMapping("/forgot")
  public ResponseEntity<AuthResponse> forgot(@Valid @RequestBody ForgotPasswordRequest req) {
    resetService.start(req.getPhone());
    return ResponseEntity.ok(new AuthResponse("PIN reset inviato via SMS", null));
  }

  @PostMapping("/verify")
  public ResponseEntity<ResetTokenResponse> verify(@Valid @RequestBody VerifyResetRequest req) {
    String resetToken = resetService.verify(req.getPhone(), req.getCode());
    return ResponseEntity.ok(new ResetTokenResponse("Codice verificato", resetToken));
  }

  @PostMapping("/reset")
  public ResponseEntity<AuthResponse> reset(@Valid @RequestBody ResetPasswordRequest req) {
    resetService.reset(req.getResetToken(), req.getNewPassword(), req.getConfirmPassword());
    return ResponseEntity.ok(new AuthResponse("Password aggiornata", null));
  }
}