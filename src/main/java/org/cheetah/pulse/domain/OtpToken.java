// domain/OtpToken.java
package org.cheetah.pulse.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document("otp_tokens")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OtpToken {
  @Id
  private String id;

  @Indexed
  private String phone;

  private String code;     // 6 cifre
  private int attempts;

  @Indexed(expireAfter = "5m")
  private Instant expiresAt;

  public static OtpToken newToken(String phone, String code, long ttlSeconds) {
    return OtpToken.builder()
        .id(UUID.randomUUID().toString())
        .phone(phone)
        .code(code)
        .attempts(0)
        .expiresAt(Instant.now().plusSeconds(ttlSeconds))
        .build();
  }
}