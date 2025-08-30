// src/main/java/com/setupnet/johndoe/dto/ResetTokenResponse.java
package org.cheetah.pulse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ResetTokenResponse {
  private String message;
  private String resetToken; // JWT corto con purpose = password_reset
}