package poller;

import static poller.JobStatus.COMPLETED;
import static poller.JobStatus.FAILED;
import static poller.JobStatus.INPROGRESS;
import static poller.JobStatus.NOTSTARTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JobFactory {

  Random random;
  double probabilityFirstStateNotStated = 0.20;
  double probabilityOfFailure = 0.15;
  double probabilityOfSuccess = 0.85;

  JobFactory() {
    random = new Random(1);
  }

  Job next(String id) {
    return new Job(id, (new JobStateMachine(createLifecycle())));
  }

  // Everything starts as INPROGRESS and finishes as COMPLETED or FAILED
  // TODO Enhance class to be able to hang and just keep HTTP connection open.
  // TODO Enhance class to model "not-yet-started".
  //    new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
  JobStatus[] createLifecycle() {
    List<JobStatus> lifecycle = new ArrayList<>();
    if (random.nextDouble() < probabilityFirstStateNotStated) {
      lifecycle.add(NOTSTARTED);
    }
    lifecycle.add(INPROGRESS);
    double p = random.nextDouble();
    if (p < probabilityOfFailure) {
      lifecycle.add(FAILED);
    } else if (p < probabilityOfSuccess) {
      lifecycle.add(COMPLETED);
    }
    // If not failure or complete, job stays stuck in progress.

    return lifecycle.toArray(new JobStatus[0]);
  }
}
