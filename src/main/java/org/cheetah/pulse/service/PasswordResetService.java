// src/main/java/com/setupnet/johndoe/service/PasswordResetService.java
package org.cheetah.pulse.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

import org.cheetah.pulse.domain.PasswordResetToken;
import org.cheetah.pulse.repo.PasswordResetTokenRepository;
import org.cheetah.pulse.repo.UserAccountRepository;
import org.cheetah.pulse.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private final PasswordResetTokenRepository resetRepo;
	private final UserAccountRepository userRepo;
	private final PhoneNormalizer phoneNormalizer;
	private final SmsSender smsSender;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	private static final SecureRandom RNG = new SecureRandom();

	@Value("${app.otp.length:6}")
	private int otpLength;
	@Value("${app.otp.ttl-seconds:300}")
	private long ttlSeconds;
	@Value("${app.otp.max-attempts:5}")
	private int maxAttempts;

	private String generateCode() {
		int max = (int) Math.pow(10, otpLength);
		int min = max / 10;
		int code = RNG.nextInt(max - min) + min;
		return String.format("%0" + otpLength + "d", code);
	}

	/** Step 1: genera e invia PIN */
	public void start(String phoneRaw) {
		String phone = phoneNormalizer.normalize(phoneRaw);
		var user = userRepo.findByPhone(phone).orElseThrow(() -> new IllegalArgumentException("Utente non trovato."));
		if (!user.isEnabled())
			throw new IllegalStateException("Account non verificato.");

		resetRepo.deleteByPhone(phone); // invalida precedenti
		var token = PasswordResetToken.newToken(phone, generateCode(), ttlSeconds);
		resetRepo.save(token);

		smsSender.send(phone, "Codice reset password: " + token.getCode());
	}

	/** Step 2: verifica PIN e restituisce un resetToken JWT */
	public String verify(String phoneRaw, String code) {
		String phone = phoneNormalizer.normalize(phoneRaw);
		var opt = resetRepo.findTopByPhoneOrderByExpiresAtDesc(phone);
		if (opt.isEmpty())
			throw new IllegalArgumentException("Nessuna richiesta di reset trovata.");
		var token = opt.get();
		if (token.getAttempts() >= maxAttempts)
			throw new ValidationException("Troppi tentativi. Richiedi un nuovo codice.");

		boolean ok = token.getCode().equals(code);
		token.setAttempts(token.getAttempts() + 1);
		resetRepo.save(token);

		if (!ok)
			throw new IllegalArgumentException("Codice non valido o scaduto.");

		// One-shot: elimina il PIN dopo verifica
		resetRepo.deleteByPhone(phone);

		var user = userRepo.findByPhone(phone).orElseThrow();
		return jwtService.generateResetToken(user.getId());
	}

	/** Step 3: imposta la nuova password usando il resetToken */
	public void reset(String resetToken, String newPassword, String confirmPassword) {
		if (!newPassword.equals(confirmPassword)) {
			throw new IllegalArgumentException("Password e conferma non coincidono.");
		}
		String userId = jwtService.parseAndValidateResetToken(resetToken);
		var user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userRepo.save(user);
	}
}