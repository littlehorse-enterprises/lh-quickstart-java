package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.util.Random;

public class KnowYourCustomerTasks {

    private static final Random random = new Random();

    @LHTaskMethod("verify-identity")
    public String verifyIdentity(String firstName, String lastName, @LHType(masked = true) int ssn) {

        if (random.nextDouble() < 0.25) {
            // Simulate an external API failure, throwing 500 status code for example
            throw new RuntimeException("The external identity verification API is down");
        }

        return "Successfully called external API to request verification for " + firstName + " " + lastName;
    }

    @LHTaskMethod("notify-customer-verified")
    public String notifyCustomerVerified(String firstName, String lastName) {
        return "Notification sent to customer " + firstName + " " + lastName + " that their identity has been verified";
    }

    @LHTaskMethod("notify-customer-not-verified")
    public String notifyCustomerNotVerified(String firstName, String lastName) {
        return "Notification sent to customer " + firstName + " " + lastName
                + " that their identity has not been verified";
    }
}
