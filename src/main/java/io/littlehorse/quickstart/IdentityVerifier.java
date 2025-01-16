package io.littlehorse.quickstart;

import java.util.Random;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;

public class IdentityVerifier {

	private static final Random random = new Random();

	@LHTaskMethod("verify-identity")
	public String verifyIdentity(String firstName, String lastName, @LHType(masked = true) int ssn) {

		if (random.nextDouble() < 0.25) {
			// Simulate an external API failure, throwing 500 status code for example
			throw new RuntimeException("The external identity verification API is down");
		}

		return "Successfully called external API";
	}
}