package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.worker.LHTaskWorker;

public class Main {

    static IdentityVerifier identityVerifier = new IdentityVerifier();
    static CustomerNotifier customerNotifier = new CustomerNotifier();
    static LHConfig config = new LHConfig();

    /*
     * This method registers the TaskDef and WfSpec to the LH Server.
     */
    private static void registerWorkflow() {
        config.getBlockingStub().putExternalEventDef(
                PutExternalEventDefRequest.newBuilder()
                        .setName("identity-verified")
                        .build());

        // Generate the TaskDef from the worker object itself
        LHTaskWorker verifyIdentityWorker = new LHTaskWorker(identityVerifier, "verify-identity", config);
        verifyIdentityWorker.registerTaskDef();

        LHTaskWorker notifyCustomerWorker = new LHTaskWorker(customerNotifier, "notify-customer", config);
        notifyCustomerWorker.registerTaskDef();

        // Since we didn't start the worker, this is a no-op, but it prevents
        // VSCode from underlining with a squiggly
        verifyIdentityWorker.close();
        notifyCustomerWorker.close();

        // Register the WfSpec
        QuickstartWorkflow quickstart = new QuickstartWorkflow();
        quickstart.getWorkflow().registerWfSpec(config.getBlockingStub());
    }

    /*
     * This method starts a Task Worker which polls the LH Server for 'greet' tasks
     * and executes them.
     */
    private static void runWorker() {

        LHTaskWorker verifyIdentityWorker = new LHTaskWorker(identityVerifier, "verify-identity", config);
        LHTaskWorker notifyCustomerWorker = new LHTaskWorker(customerNotifier, "notify-customer", config);

        // Close the worker upon shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(verifyIdentityWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(notifyCustomerWorker::close));

        System.out.println("Starting task workers!");
        verifyIdentityWorker.start();
        notifyCustomerWorker.start();
    }

    public static void main(String[] args) {
        if (args.length != 1 || (!args[0].equals("register") && !args[0].equals("worker"))) {
            System.err.println("Please provide one argument: either 'register' or 'worker'");
            System.exit(1);
        }

        if (args[0].equals("register")) {
            registerWorkflow();
        } else {
            runWorker();
        }
    }
}
