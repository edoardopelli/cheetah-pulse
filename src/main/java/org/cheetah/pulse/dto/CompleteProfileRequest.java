// src/main/java/com/setupnet/johndoe/dto/CompleteProfileRequest.java
package org.cheetah.pulse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CompleteProfileRequest {

  @Size(min = 1, max = 100)
  @Schema(example = "Mario")
  private String name;

  @Size(min = 1, max = 100)
  @Schema(example = "Rossi")
  private String surname;

  @Email
  @Schema(example = "mario.rossi@example.com")
  private String email;

  @JsonFormat(pattern = "yyyy-MM-dd")
  @Schema(example = "1985-02-10", type = "string", format = "date")
  private LocalDate birthDate;
}