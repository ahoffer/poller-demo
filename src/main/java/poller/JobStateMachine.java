package poller;

public class JobStateMachine {

  static double probabilityOfStateChange = 0.20;

  final JobStatus[] lifecycle;
  int index;

  public JobStateMachine(JobStatus[] lifecycle) {
    this.lifecycle = lifecycle;
  }

  JobStatus getStatus() {
    return lifecycle[index];
  }

  JobStatus tryAdvanceAndGet(double p) {
    advance(p);
    return getStatus();
  }

  private void advance(double p) {
    if (!isFinal() && p < probabilityOfStateChange) {
      index++;
    }
  }

  boolean isFinal() {
    int maxIndex = lifecycle.length - 1;
    return index >= maxIndex;
  }
}
