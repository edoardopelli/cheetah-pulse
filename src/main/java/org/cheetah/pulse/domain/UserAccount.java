// domain/UserAccount.java
package org.cheetah.pulse.domain;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
	@Id
	private String id;

	@Indexed(unique = true)
	private String phone; // E.164 (+39...)

	private String name;
	private String surname;
	private String passwordHash;

	private LocalDate birthDate;

	private boolean enabled;

	private String email;

	private Instant createdAt;
	private Instant verifiedAt;
}