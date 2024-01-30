package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;

import java.io.IOException;


public class Main {

    /*
     * Returns an LHConfig and loads configuration from environment variables.
     */
    public static LHConfig getConfig() {
        return new LHConfig();
    }

    /*
     * This method registers the TaskDef and WfSpec to the LH Server.
     */
    private static void registerWorkflow() throws IOException {
        LHConfig config = getConfig();
        
        // Generate the TaskDef from the worker object itself
        Greeter greeter = new Greeter();
        LHTaskWorker worker = new LHTaskWorker(greeter, "greet", config);
        worker.registerTaskDef();

        // Since we didn't start the worker, this is a no-op, but it prevents
        // VSCode from underlining with a squiggly
        worker.close();

        // Register the WfSpec
        QuickstartWorkflow quickstart = new QuickstartWorkflow();
        quickstart.getWorkflow().registerWfSpec(config.getBlockingStub());
    }

    /*
     * This method starts a Task Worker which polls the LH Server for 'greet' tasks
     * and executes them.
     */
    private static void runWorker() throws IOException {
        Greeter greeter = new Greeter();
        LHTaskWorker worker = new LHTaskWorker(greeter, "greet", getConfig());

        // Close the worker upon shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));

        System.out.println("Starting task worker!");
        worker.start();
    }

    public static void main(String[] args) throws IOException {
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
