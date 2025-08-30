// src/main/java/com/setupnet/johndoe/domain/PasswordResetToken.java
package org.cheetah.pulse.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document("password_reset_tokens")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PasswordResetToken {
  @Id
  private String id;

  @Indexed
  private String phone;

  /** PIN numerico (6 cifre di default) */
  private String code;

  private int attempts;

  /** TTL index */
  @Indexed(expireAfterSeconds = 0)
  private Instant expiresAt;

  public static PasswordResetToken newToken(String phone, String code, long ttlSeconds) {
    return PasswordResetToken.builder()
        .id(UUID.randomUUID().toString())
        .phone(phone)
        .code(code)
        .attempts(0)
        .expiresAt(Instant.now().plusSeconds(ttlSeconds))
        .build();
  }
}