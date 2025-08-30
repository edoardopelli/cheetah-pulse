// src/main/java/com/setupnet/johndoe/web/MePasswordController.java
package org.cheetah.pulse.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import static org.cheetah.pulse.config.OpenApiConfig.BEARER_SCHEME;

import org.cheetah.pulse.dto.AuthResponse;
import org.cheetah.pulse.dto.ChangePasswordRequest;
import org.cheetah.pulse.service.PasswordChangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MePasswordController {

  private final PasswordChangeService passwordChangeService;

  @Operation(
      summary = "Cambio password (utente autenticato)",
      description = "Non richiede la vecchia password. Serve JWT valido.",
      security = {@SecurityRequirement(name = BEARER_SCHEME)}
  )
  @PatchMapping("/me/password")
  public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) return ResponseEntity.status(401).build();

    String userId = auth.getPrincipal().toString();
    passwordChangeService.changePassword(userId, req.getNewPassword(), req.getConfirmPassword());
    return ResponseEntity.ok(new AuthResponse("Password aggiornata", null));
  }
}