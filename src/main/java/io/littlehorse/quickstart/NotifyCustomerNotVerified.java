package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class NotifyCustomerNotVerified {

    @LHTaskMethod("notify-customer-not-verified")
    public String notifyCustomerNotVerified(String firstName, String lastName) {
        return "Notification sent to customer " + firstName + " " + lastName
                + " that their identity has not been verified";
    }
}
