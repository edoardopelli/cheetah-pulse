// src/main/java/com/setupnet/johndoe/dto/ForgotPasswordRequest.java
package org.cheetah.pulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
  @NotBlank
  @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$")
  private String phone;
}