// dto/AuthResponse.java
package org.cheetah.pulse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class AuthResponse {
  private String message;
  private String token; // JWT
}