// src/main/java/com/setupnet/johndoe/dto/MeResponse.java
package org.cheetah.pulse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

import org.cheetah.pulse.domain.UserAccount;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeResponse {
  @Schema(example = "665f0c5c9b2a4a3e8c1d1234")
  private String id;

  @Schema(example = "+393401234567")
  private String phone;

  @Schema(example = "Mario")
  private String name;

  @Schema(example = "Rossi")
  private String surname;

  @Schema(example = "mario.rossi@example.com")
  private String email;

  @Schema(example = "1985-02-10", type = "string", format = "date")
  private LocalDate birthDate;

  private boolean enabled;

  private Instant createdAt;
  private Instant verifiedAt;

  public static MeResponse from(UserAccount u) {
    return new MeResponse(
        u.getId(),
        u.getPhone(),
        u.getName(),
        u.getSurname(),
        u.getEmail(),
        u.getBirthDate(),
        u.isEnabled(),
        u.getCreatedAt(),
        u.getVerifiedAt()
    );
  }
}