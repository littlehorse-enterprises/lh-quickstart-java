package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class NotifyCustomerVerified {
	@LHTaskMethod("notify-customer-verified")
	public String notifyCustomerVerified() {
		return "Notification sent to customer that their identity has been verified";
	}
}
