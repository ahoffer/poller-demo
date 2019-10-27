package poller;

public class JobStateMachine {

  JobStatus[] lifecycle;
  int index;

  public JobStateMachine(JobStatus[] lifecycle, double probabilityOfStateChange) {
    this.lifecycle = lifecycle;
    this.probabilityOfStateChange = probabilityOfStateChange;
    index = 0;
  }

  double probabilityOfStateChange;

  JobStatus getStatus() {
    return lifecycle[index];
  }

  JobStatus tryAdvanceAndGet(double p) {
    advance(p);
    return getStatus();
  }

  private void advance(double p) {
    if (!isFinal() && probabilityOfStateChange > p) {
      index++;
    }
  }

  boolean isFinal() {
    int maxIndex = lifecycle.length - 1;
    return index >= maxIndex;
  }
}
