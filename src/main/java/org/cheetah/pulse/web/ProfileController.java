// src/main/java/com/setupnet/johndoe/web/ProfileController.java
package org.cheetah.pulse.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import static org.cheetah.pulse.config.OpenApiConfig.BEARER_SCHEME;

import org.cheetah.pulse.dto.CompleteProfileRequest;
import org.cheetah.pulse.dto.MeResponse;
import org.cheetah.pulse.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  @Operation(
      summary = "Completa i campi mancanti del profilo",
      description = "Aggiorna solo i campi null/vuoti: name, surname, email, birthDate",
      security = {@SecurityRequirement(name = BEARER_SCHEME)}
  )
  @PatchMapping("/profile")
  public ResponseEntity<MeResponse> completeProfile(@RequestBody CompleteProfileRequest req) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) return ResponseEntity.status(401).build();

    String userId = auth.getPrincipal().toString();
    var user = profileService.completeMissing(userId, req);

    return ResponseEntity.ok(MeResponse.from(user));
  }
}