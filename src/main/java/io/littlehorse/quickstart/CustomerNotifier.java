package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class CustomerNotifier {

	@LHTaskMethod("notify-customer")
	public String notifyCustomer(Boolean identityVerified) {
		if (identityVerified) {
			return "Notification sent: Identity verified";
		} else {
			return "Notification sent: Identity not verified";
		}
	}
}