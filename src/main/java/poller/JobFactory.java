package poller;

import static poller.JobStatus.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JobFactory {

  Random random;
  double probabilityOfStateChange;
  double probabilityOfFailure;

  JobFactory() {
    random = new Random(1);
    probabilityOfStateChange = 0.10;
    probabilityOfFailure = 0.10;
  }

  Job next(String id) {
    return new Job(id, (new JobStateMachine(createLifecycle(), probabilityOfStateChange)));
  }

  // Everything starts as INPROGRESS and finishes as COMPLETED or FAILED
  // TODO Enhance class to be able to hang and just keep HTTP connection open.
  // TODO Enhance class to model "not-yet-started".
  //    new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
  JobStatus[] createLifecycle() {
    List<JobStatus> lifecycle = new ArrayList<>();
    lifecycle.add(INPROGRESS);
    lifecycle.add(random.nextDouble() < probabilityOfFailure ? FAILED : COMPLETED);
    return lifecycle.toArray(new JobStatus[0]);
  }
}
