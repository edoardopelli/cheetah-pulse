// dto/LoginRequest.java
package org.cheetah.pulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {
  @NotBlank
  @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$")
  private String phone;

  @NotBlank
  private String password;
}