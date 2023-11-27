package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.proto.IndexType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class QuickstartWorkflow {

    public static final String WF_NAME = "quickstart";
    public static final String GREET = "greet";

    /*
     * This method defines the logic of our workflow
     */
    public void quickstartWf(WorkflowThread wf) {
        // Create an input variable
        WfRunVariable name = wf.addVariable("input-name", VariableType.STR);

        // Make the variable searchable
        name.withIndex(IndexType.LOCAL_INDEX).persistent();

        // Execute a task and pass in the variable.
        wf.execute(GREET, name);
    }

    /*
     * This method returns a LittleHorse `Workflow` wrapper object that can be
     * used to register the WfSpec to the LH Server.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow(WF_NAME, this::quickstartWf);
    }
}
