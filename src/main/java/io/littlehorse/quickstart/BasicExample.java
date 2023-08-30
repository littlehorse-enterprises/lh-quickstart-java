package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/*
 * This is a simple example, which does two things:
 * 1. Declares an "input-name" variable of type String
 * 2. Passes that variable into the execution of the "greet" task.
 */
public class BasicExample {

    private static final Logger log = LoggerFactory.getLogger(BasicExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-basic",
            thread -> {
                WfRunVariable theName = thread.addVariable(
                    "input-name",
                    VariableType.STR
                );
                thread.execute("greet", theName);
            }
        );
    }

    public static Properties getConfigProps() {
        Properties props = new Properties();
        props.put("LHC_API_HOST", "localhost");
        props.put("LHC_API_PORT", "2023");
        return props;
    }

    public static LHTaskWorker getTaskWorker(LHConfig config) throws IOException {
        MyWorker executable = new MyWorker();
        LHTaskWorker worker = new LHTaskWorker(executable, "greet", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        LHTaskWorker worker = getTaskWorker(config);

        // Register task if it does not exist
        if (worker.doesTaskDefExist()) {
            log.debug(
                "Task {} already exists, skipping creation",
                worker.getTaskDefName()
            );
        } else {
            log.debug(
                "Task {} does not exist, registering it",
                worker.getTaskDefName()
            );
            worker.registerTaskDef();
        }

        // Register a workflow if it does not exist
        if (workflow.doesWfSpecExist(config.getBlockingStub())) {
            log.debug(
                "Workflow {} already exists, skipping creation",
                workflow.getName()
            );
        } else {
            log.debug(
                "Workflow {} does not exist, registering it",
                workflow.getName()
            );
            workflow.registerWfSpec(config.getBlockingStub());
        }

        // Run the worker
        worker.start();
    }
}
