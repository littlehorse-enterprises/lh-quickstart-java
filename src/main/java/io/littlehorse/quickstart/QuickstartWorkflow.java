package io.littlehorse.quickstart;

import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class QuickstartWorkflow {

    /*
     * This method defines the logic of our workflow
     */
    public void quickstartWf(WorkflowThread wf) {
        // Create an input variable, make it searchable
        WfRunVariable firstName = wf.declareStr("firstName").searchable().required();
        WfRunVariable lastName = wf.declareStr("lastName").searchable().required();
        WfRunVariable ssn = wf.declareInt("ssn").masked().required();

        // Call the verify-identity task and retry it up to 3 times if it fails
        wf.execute("verify-identity", firstName, lastName, ssn).withRetries(3);

        // This is a blocking call, so it will wait until the event is posted or
        // if the timeout is reached
        NodeOutput identityPosted = wf.waitForEvent("identity-verified").timeout(60 * 60 * 24 * 3);

        wf.execute("notify-customer", identityPosted);
    }

    /*
     * This method returns a LittleHorse `Workflow` wrapper object that can be
     * used to register the WfSpec to the LH Server.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow("quickstart", this::quickstartWf);
    }
}
