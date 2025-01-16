package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class QuickstartWorkflow {

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
        wf.execute("verify-identity", firstName, lastName, ssn).withRetries(3);

        // This is a blocking call, so it will wait until the event is posted or
        // if the timeout is reached
        NodeOutput identityPosted = wf.waitForEvent("identity-verified").timeout(60 * 60 * 24 * 3);

        // Mutate the identityVerified variable to true if the event was posted
        wf.mutate(identityVerified, VariableMutationType.ASSIGN, identityPosted);

        // Notify the customer if their identity was verified or not
        wf.doIfElse(wf.condition(identityVerified, Comparator.EQUALS, true),
                (ifBody) -> {
                    ifBody.execute("notify-customer-verified");
                }, (elseBody) -> {
                    elseBody.execute("notify-customer-not-verified");
                });
    }

    /*
     * This method returns a LittleHorse `Workflow` wrapper object that can be
     * used to register the WfSpec to the LH Server.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow("quickstart", this::quickstartWf);
    }
}
