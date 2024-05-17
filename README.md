<p align="center">
<img alt="LH" src="https://littlehorse.dev/img/logo.jpg" width="50%">
</p>

# LittleHorse Java Quickstart

- [LittleHorse Java Quickstart](#littlehorse-java-quickstart)
- [Prerequisites](#prerequisites)
  - [LittleHorse CLI](#littlehorse-cli)
  - [Local LH Server Setup](#local-lh-server-setup)
  - [Verifying Setup](#verifying-setup)
- [Running the Example](#running-the-example)
  - [Register Workflow](#register-workflow)
  - [Run Workflow](#run-workflow)
  - [Run Task Worker](#run-task-worker)
- [Advanced Topics](#advanced-topics)
  - [Inspect the TaskRun](#inspect-the-taskrun)
  - [Search for Someone's Workflow](#search-for-someones-workflow)
  - [NodeRuns and TaskRuns](#noderuns-and-taskruns)
  - [Debugging Errors](#debugging-errors)
- [Next Steps](#next-steps)

**Get started in under 5 minutes, or your money back!** :wink:

This repo contains a minimal example to get you started using LittleHorse in Java. [LittleHorse](www.littlehorse.dev) is a high-performance orchestration engine which lets you build workflow-driven microservice applications with ease.

You can run this example in two ways:

1. Using a local deployment of a LittleHorse Server (instructions below, requires one `docker` command).
2. Using a LittleHorse Server deployed in a cloud sandbox (to get one, contact `info@littlehorse.io`).

In this example, we will run a classic "Greeting" workflow as a quickstart. The workflow takes in one input variable (`input-name`), and calls a `greet` Task Function with the specified `input-name` as input.

# Prerequisites

Your system needs:

- Java 11 or greater
- `docker` to run the LH Server, OR access to a private LH Cloud Sandbox.
- Homebrew (tested on Mac or Linux) to install `lhctl`

This example uses Gradle to compile the Java code, but you can get around that dependency by using `./gradlew` which wraps the gradle binary in a Jar file and relies upon your system installation of Java.

## LittleHorse CLI

Install the LittleHorse CLI:

```
brew install littlehorse-enterprises/lh/lhctl
```

Alternatively, if you have `go` but don't have homebrew, you can:

```
go install github.com/littlehorse-enterprises/littlehorse/lhctl@0.9.0
```

## Local LH Server Setup

If you have obtained a private LH Cloud Sandbox, you can skip this step and just follow the configuration instructions you received from the LittleHorse Team (remember to set your environment variables!).

To run a LittleHorse Server locally in one command, you can run:

```
docker run --name littlehorse -d -p 2023:2023 -p 8080:8080 ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:0.9.0
```

Using the local LittleHorse Server takes about 15-25 seconds to start up, but it does not require any further configuration. Please note that the `lh-standalone` docker image requires at least 1.5GB of memory to function properly. This is because it runs kafka, the LH Server, and the LH Dashboard (2 JVM's and a NextJS app) all in one container.

## Verifying Setup

At this point, whether you are using a local Docker deployment or a private LH Cloud Sandbox, you should be able to contact the LH Server:

```
->lhctl version
lhctl version: 0.9.0
Server version: 0.9.0
```

**You should also be able to see the dashboard** at `https://localhost:8080`. It should be empty, but we will put some data in there soon when we run the workflow!

If you _can't_ get the above to work, please let us know at `info@littlehorse.io`. We will create a community slack for support soon.

# Running the Example

Without further ado, let's run the example start-to-finish.

If you haven't done so already, at this point go ahead and clone this repository to your local machine.

## Register Workflow

Let's run the `Main` app with the `register` argument, which does two things:

1. Registers a `TaskDef` named `greet` with LittleHorse.
2. Registers a `WfSpec` named `quickstart` with LittleHorse.

A [`WfSpec`](https://littlehorse.dev/docs/concepts/workflows) specifies a process which can be orchestrated by LittleHorse. A [`TaskDef`](https://littlehorse.dev/docs/concepts/tasks) tells LittleHorse about a specification of a task that can be executed as a step in a `WfSpec`.

```
./gradlew run --args register
```

You can inspect your `WfSpec` with `lhctl` as follows. It's ok if the response doesn't make sense, we will see it soon!

```bash
lhctl get wfSpec quickstart
```

Now, go to your dashboard in your browser (`http://localhost:8080`) and refresh the page. Click on the `quickstart` WfSpec. You should see something that looks like a flow-chart. That is your Workflow Specification!

## Run Workflow

Now, let's run our first `WfRun`! Use `lhctl` to run an instance of our `WfSpec`. 

```
# Run the 'quickstart' WfSpec, and set 'input-name' = "obi-wan"
lhctl run quickstart input-name obi-wan
```

The response prints the initial status of the `WfRun`. Pull out the `id` and copy it!

Let's look at our `WfRun` once again. To do it with the CLI, please run:

```
lhctl get wfRun <wf_run_id>
```

If you would like to see it on the dashboard, refresh the `WfSpec` page and scroll down. You should see your ID under the `RUNNING` column. Please double-click on your `WfRun` id, and it will take you to the `WfRun` page.

Note that the status is `RUNNING`! Why hasn't it completed? That's because we haven't yet started a worker which executes the `greet` tasks. Want to verify that? Let's search for all tasks in the queue which haven't been executed yet. You should see an entry whose `wfRunId` matches the Id from above:

```
lhctl search taskRun --taskDefName greet --status TASK_SCHEDULED
```

You can also see the `TaskRun` node on the workflow. It's highlighted, meaning that it's already running! If you click on it, you can see that it is in the `TASK_SCHEDULED` status.

## Run Task Worker

Now let's start our worker, so that our blocked `WfRun` can finish. What this does is start a daemon which calls the `Greeter#greet()` Java Method for every scheduled `TaskRun` with appropriate parameters.

```
./gradlew run --args worker
```

Once the worker starts up, please open another terminal and inspect our `WfRun` again:

```
lhctl get wfRun <wf_run_id>
```

Voila! It's completed. You can also verify that the Task Queue is empty now that the Task Worker executed all of the tasks:

```
lhctl search taskRun --taskDefName greet --status TASK_SCHEDULED
```

Please refresh the dashboard, and you can see the `WfRun` has been completed!

# Advanced Topics

You have now passed the requirements to reach the level of Jedi Youngling. Want to become a Padawan, or even a Knight? Then keep reading!

Here are some cool commands which scratch the surface of observability offered to you by LittleHorse. Note that you can also access this information via the dashboard at `https://localhost:8080` to do this via click-ops, but in the following section we will show you how to interact with littlehorse through bash-ops.

Also, note that everything we are doing here can be done programmatically via our SDK's, but it's easier to demonstrate with `lhctl`.

## Inspect the TaskRun

Let's find the completed `TaskRun`:

```
lhctl search taskRun --taskDefName greet --status TASK_SUCCESS
```

Take the output from above, and inspect it! Notice that you can see the input variables and also the output, which is a greeting string.

```
lhctl get taskRun <wf_run_id> <task_guid>
```

## Search for Someone's Workflow

Remember we passed an `input-name` variable to our workflow? If you look in `QuickstartWorkflow.java`, specifically the `quickstartWf()` function, you can see that we created an Index on the variable by registering it with `.searchable()`. This means we can search for variables by their value!

```
lhctl search variable --varType STR --wfSpecName quickstart --name input-name --value obi-wan
```

And the following should return an empty list (unless, of course, you do `lhctl run quickstart input-name asdfasdf`)

```
lhctl search variable --varType STR --wfSpecName quickstart --name input-name --value asdfasdf
```

## NodeRuns and TaskRuns

Let's look at our `WfRun`:

```
-> lhctl get wfRun <wfRunId>
{
  "id": "4a139cd6326944d8a2f2021385a259e0",
  "wfSpecName": "quickstart",
  "wfSpecVersion": 0,
  "status": "COMPLETED",
  "startTime": "2023-10-15T04:56:26.292Z",
  "endTime": "2023-10-15T04:56:57.158Z",
  "threadRuns": [
    {
      "number": 0,
      "status": "COMPLETED",
      "threadSpecName": "entrypoint",
      "startTime": "2023-10-15T04:56:26.350Z",
      "endTime": "2023-10-15T04:56:57.154Z",
      "childThreadIds": [],
      "haltReasons": [],
      "currentNodePosition": 2,
      "handledFailedChildren": [],
      "type": "ENTRYPOINT"
    }
  ],
  "pendingInterrupts": [],
  "pendingFailures": []
}
```

There are a few things to note:
* The `status` is `COMPLETED`
* There is one `ThreadRun`. That makes sense, since we didn't add multi-threading to the `WfRun`.
* The `currentNodePosition` is 2.

What is a `NodeRun`? A `NodeRun` is a step in a `ThreadRun`. Our workflow's main `ThreadRun` has three steps:

1. The `ENTRYPOINT` node
2. The `TASK` node to execute the `greet` task
3. The `EXIT` node, which wraps things up.

Let's see all of our nodes via:

```
lhctl list nodeRun <wfRunId>
```

Note that the second `nodeRun` has a `task` field, points to the `TaskRun` we saw earlier. You can find it via:

```
lhctl get taskRun <wfRunId> <taskGuid>
```

## Debugging Errors

What happens if a Task Run fails? Edit `Greeter.java` and make the `greeting()` function throw an error of choice (maybe `throw new RuntimeException("asdf")` or something like that). Then, restart the worker via `./gradlew run --args worker`.

Run another workflow:

```
lhctl run quickstart input-name anakin
```

Then, `lhctl get wfRun <wfRunId>` should show that the workflow failed. It should also show that `currentNodePosition` for `ThreadRun` `0` is `1`. Let's inspect the NodeRun:

```
lhctl get nodeRun <wfRunId> 0 1
```

It's a `TaskRun`! Let's see what happened:

```
lhctl get taskRun <wfRunId> <taskGuid>
```

As you can see, you can get the stack trace through the LittleHorse API.

You can also find the `TaskRun` by searching for failed tasks. Remember that all of this is also presented in our super-cool dashboard.

```
lhctl search taskRun --taskDefName greet --status TASK_FAILED

# or search for workflows by their status
lhctl search wfRun --wfSpecName quickstart --status ERROR
lhctl search wfRun --wfSpecName quickstart --status COMPLETED
```

If you want to handle such failures in your workflow, check our [exception handling documentation](www.littlehorse.dev/docs/concepts/exception-handling).

# Next Steps

If you've made it this far, then it's time you become a full-fledged LittleHorse Knight!

Want to do more cool stuff with LittleHorse and Java? You can find more Java examples [here](https://github.com/littlehorse-enterprises/littlehorse/tree/master/examples). This example only shows rudimentary features like tasks and variables. Some additional features not covered in this quickstart include:

* Conditionals
* Loops
* External Events (webhooks/signals etc)
* Interrupts
* User Tasks
* Multi-Threaded Workflows
* Workflow Exception Handling

We also have quickstarts in [Python](https://github.com/littlehorse-enterprises/lh-quickstart-python) and [Go](https://github.com/littlehorse-enterprises/lh-quickstart-go). Support for .NET is coming soon.

Our extensive [documentation](www.littlehorse.dev) explains LittleHorse concepts in detail and shows you how take full advantage of our system.

Our LittleHorse Server is free for production use under the SSPL license. You can find our official docker image at the [AWS ECR Public Gallery](https://gallery.ecr.aws/littlehorse/lh-server). If you would like enterprise support, or a managed service (either in the cloud or on-prem), contact `info@littlehorse.io`.

Happy riding!
