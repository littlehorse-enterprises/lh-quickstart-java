package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.worker.LHTaskWorker;

public class Main {

    static KnowYourCustomerTasks tasks = new KnowYourCustomerTasks();

    static LHConfig config = new LHConfig();

    /*
     * This method registers the TaskDef and WfSpec to the LH Server.
     */
    private static void registerMetadata() {
        // We must register the `ExternalEventDef` for `identity-verified` before we can
        // use it in a `WfSpec`.
        config.getBlockingStub()
                .putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                        .setName("identity-verified")
                        .build());

        // We must also register the TaskDefs before we can use them in a `WfSpec`.
        LHTaskWorker verifyIdentityWorker = new LHTaskWorker(tasks, "verify-identity", config);
        verifyIdentityWorker.registerTaskDef();

        LHTaskWorker notifyCustomerVerifiedWorker = new LHTaskWorker(tasks, "notify-customer-verified", config);
        notifyCustomerVerifiedWorker.registerTaskDef();

        LHTaskWorker notifyCustomerNotVerifiedWorker = new LHTaskWorker(tasks, "notify-customer-not-verified", config);
        notifyCustomerNotVerifiedWorker.registerTaskDef();

        // Since we didn't start the worker, this is a no-op, but it prevents
        // VSCode from underlining with a squiggly
        verifyIdentityWorker.close();
        notifyCustomerVerifiedWorker.close();
        notifyCustomerNotVerifiedWorker.close();

        // After registering the other metadata, we can finally register the WfSpec
        QuickstartWorkflow quickstart = new QuickstartWorkflow();
        quickstart.getWorkflow().registerWfSpec(config.getBlockingStub());
    }

    /*
     * This method starts a Task Worker which polls the LH Server for 'greet' tasks
     * and executes them.
     */
    private static void startTaskWorkers() {

        LHTaskWorker verifyIdentityWorker = new LHTaskWorker(tasks, "verify-identity", config);
        LHTaskWorker notifyCustomerVerifiedWorker = new LHTaskWorker(tasks, "notify-customer-verified", config);
        LHTaskWorker notifyCustomerNotVerifiedWorker = new LHTaskWorker(tasks, "notify-customer-not-verified", config);

        // Close the worker upon shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(verifyIdentityWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(notifyCustomerVerifiedWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(notifyCustomerNotVerifiedWorker::close));

        System.out.println("Starting task workers!");
        verifyIdentityWorker.start();
        notifyCustomerVerifiedWorker.start();
        notifyCustomerNotVerifiedWorker.start();
    }

    public static void main(String[] args) {
        if (args.length != 1 || (!args[0].equals("register") && !args[0].equals("worker"))) {
            System.err.println("Please provide one argument: either 'register' or 'worker'");
            System.exit(1);
        }

        if (args[0].equals("register")) {
            registerMetadata();
        } else {
            startTaskWorkers();
        }
    }
}
