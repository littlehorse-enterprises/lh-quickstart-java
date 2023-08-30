<p align="center">
<img alt="LH" src="https://littlehorse.dev/img/logo.jpg" width="50%">
</p>

## Quickstart for Java

### Prerequisites

- Java 11 or greater
- `docker`
- `go`

### Running Locally

Install `lhctl`:

```
go install github.com/littlehorse-enterprises/littlehorse/lhctl@latest
```

Verify the installation:

```
lhctl
```

Start a LH Server with:

```
docker run --name littlehorse -d -p 2023:2023 public.ecr.aws/k7z9g8i4/littlehorse-standalone:latest
```

When you run the LH Server according to the command above, the API Host is `localhost` and the API Port is `2023`.
Now configure `~/.config/littlehorse.config`:

```
LHC_API_HOST=localhost
LHC_API_PORT=2023
```

You can confirm that the Server is running via:

```
lhctl search wfSpec
```

Result:

```
{
  "results": []
}
```

Now let's run an example

```
./gradlew run
```

In another terminal, use `lhctl` to run the workflow:

```
# Here, we specify that the "input-name" variable = "Obi-Wan"
lhctl run example-basic input-name Obi-Wan
```

Now let's inspect the result:

```
# This call shows the workflow specification
lhctl get wfSpec example-basic

# This call shows the result
lhctl get wfRun <wf run id>

# Inspect the first NodeRun of the WfRun
lhctl get nodeRun <wf run id> 0 1

# This shows the task run information
lhctl get taskRun <wfRunId> <taskGuid>
```

> More Java examples [here](https://github.com/littlehorse-enterprises/littlehorse/tree/master/examples).
