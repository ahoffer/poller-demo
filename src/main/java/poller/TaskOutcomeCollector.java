package poller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
/**
 * This class encapsulates the logic to report about the state of the polling tasks (as opposed to
 * the results of the Job they are polling). This class is meant to be free of IO, i.e. no logging
 */
public class TaskOutcomeCollector {

  private final List<Future<String>> tasks;

  public TaskOutcomeCollector(List<Future<String>> tasks) {
    this.tasks = Collections.unmodifiableList(tasks);
  }

  Map<String, Long> get() {
    return Map.of("DONE", numDone(), "CANCELLED", numCancelled(), "NOT DONE", numNotDone());
  }

  public long numNotDone() {
    return tasks.size() - numDone() - numCancelled();
  }

  public long numCancelled() {
    return tasks.stream().filter(Future::isCancelled).count();
  }

  public long numDone() {
    return tasks.stream().filter(Future::isDone).count();
  }
}
