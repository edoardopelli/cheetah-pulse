// src/main/java/com/setupnet/johndoe/dto/ResetPasswordRequest.java
package org.cheetah.pulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
  @NotBlank
  private String resetToken;

  @NotBlank @Size(min = 8, max = 100)
  private String newPassword;

  @NotBlank @Size(min = 8, max = 100)
  private String confirmPassword;
}