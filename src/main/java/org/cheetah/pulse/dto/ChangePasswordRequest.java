// src/main/java/com/setupnet/johndoe/dto/ChangePasswordRequest.java
package org.cheetah.pulse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
  @NotBlank
  @Size(min = 8, max = 100)
  @Schema(example = "NuovaPassw0rd!")
  private String newPassword;

  @NotBlank
  @Size(min = 8, max = 100)
  @Schema(example = "NuovaPassw0rd!")
  private String confirmPassword;
}