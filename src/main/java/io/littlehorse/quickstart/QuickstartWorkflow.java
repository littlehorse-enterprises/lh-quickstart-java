package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class QuickstartWorkflow {
    final String WORKFLOW_NAME = "quickstart";
    final String IDENTITY_VERIFIED_EVENT = "identity-verified";
    final String VERIFY_IDENTITY_TASK = "verify-identity";
    final String NOTIFY_CUSTOMER_VERIFIED_TASK = "notify-customer-verified";
    final String NOTIFY_CUSTOMER_NOT_VERIFIED_TASK = "notify-customer-not-verified";

    /*
     * This method defines the logic of our workflow
     */
    public void quickstartWf(WorkflowThread wf) {
        // Declare the input variables for the workflow
        WfRunVariable firstName = wf.declareStr("firstName").searchable().required();
        WfRunVariable lastName = wf.declareStr("lastName").searchable().required();
        WfRunVariable ssn = wf.declareInt("ssn").masked().required();

        // This variable will be used internally to track the identity verification
        // status and will be able to search for any workflows that have the cusotmer's
        // identity not verified
        WfRunVariable identityVerified = wf.declareBool("identityVerified").searchable();

        // Call the verify-identity task and retry it up to 3 times if it fails
        wf.execute(VERIFY_IDENTITY_TASK, firstName, lastName, ssn).withRetries(3);

        // This is a blocking call, so it will wait until the event is posted or
        // if the timeout is reached
        NodeOutput identityVerificationResult = wf.waitForEvent(IDENTITY_VERIFIED_EVENT).timeout(60 * 60 * 24 * 3);

        wf.handleError(identityVerificationResult, LHErrorType.TIMEOUT, handler -> {
            handler.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, firstName, lastName);
            handler.fail("customer-not-verified", "Unable to verify customer identity in time.");
        });

        // Assign the output of the ExternalEvent to the `identityVerified` variable.
        identityVerified.assign(identityVerificationResult);

        // Notify the customer if their identity was verified or not
        wf.doIfElse(wf.condition(identityVerified, Comparator.EQUALS, true),
                (ifBody) -> {
                    ifBody.execute(NOTIFY_CUSTOMER_VERIFIED_TASK, firstName, lastName);
                }, (elseBody) -> {
                    elseBody.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, firstName, lastName);
                });
    }

    /*
     * This method returns a LittleHorse `Workflow` wrapper object that can be
     * used to register the WfSpec to the LH Server.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow(WORKFLOW_NAME, this::quickstartWf);
    }
}
