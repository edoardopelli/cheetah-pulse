// src/main/java/com/setupnet/johndoe/dto/VerifyResetRequest.java
package org.cheetah.pulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyResetRequest {
  @NotBlank
  @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$")
  private String phone;

  @NotBlank @Size(min = 6, max = 6)
  @Pattern(regexp = "^[0-9]{6}$")
  private String code;
}