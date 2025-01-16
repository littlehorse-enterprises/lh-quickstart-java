package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class NotifyCustomerVerified {

    @LHTaskMethod("notify-customer-verified")
    public String notifyCustomerVerified(String firstName, String lastName) {
        return "Notification sent to customer " + firstName + " " + lastName + " that their identity has been verified";
    }
}
