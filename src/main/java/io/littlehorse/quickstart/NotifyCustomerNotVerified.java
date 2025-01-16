package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class NotifyCustomerNotVerified {
	@LHTaskMethod("notify-customer-not-verified")
	public String notifyCustomerNotVerified() {
		return "Notification sent to customer that their identity has not been verified";
	}
}
