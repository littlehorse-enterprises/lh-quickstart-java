package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Greeter {

    private static final Logger log = LoggerFactory.getLogger(Greeter.class);

    /*
     * The @LHTaskMethod annotation here tells LittleHorse to call this method
     * every time a `greet` TaskRun is scheduled.
     */
    @LHTaskMethod("greet")
    public String greet(String name) {
        log.info("Executing task greet for input {}", name);
        return "hello there, " + name;
    }
}
