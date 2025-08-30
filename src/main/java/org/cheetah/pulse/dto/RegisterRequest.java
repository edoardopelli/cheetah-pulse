// dto/RegisterRequest.java
package org.cheetah.pulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
  @NotBlank
  @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Telefono deve essere in formato E.164")
  private String phone;

  @NotBlank @Size(min = 8, max = 100)
  private String password;

  @NotBlank @Size(min = 8, max = 100)
  private String confirmPassword;
}