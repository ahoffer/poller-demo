package poller;

import java.util.Random;

public class Job {

  final String id;
  final JobStateMachine stateMachine;
  static final Random random = new Random(2);

  public Job(String id, JobStateMachine stateMachine) {
    this.id = id;
    this.stateMachine = stateMachine;
  }

  public String getCurrentStatus() {
    return stateMachine.tryAdvanceAndGet(random.nextDouble()).toString();
  }

  // Use this to clean up the list of jobs and generate some metrics
  public boolean isFinal() {
    return stateMachine.isFinal();
  }
}
