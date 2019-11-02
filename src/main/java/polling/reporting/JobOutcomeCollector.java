package polling.reporting;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * WARNING. This method attempts to get the value of any Future that is done.
 *
 * <p>This class encapsulates the logic to report about the Jobs the status endpoint is serving (as
 * opposed to the local tasks that are actually polling the status endpoint). This class is meant to
 * be free of IO, i.e. no logging
 */
class JobOutcomeCollector {

  private final List<Future<String>> tasks;

  public JobOutcomeCollector(List<Future<String>> tasks) {
    this.tasks = Collections.unmodifiableList(tasks);
  }

  public Map<String, Long> get() {
    Map<String, Long> map =
        tasks.stream()
            .map(this::saferGet)
            .filter(Objects::nonNull)
            .collect(groupingBy(identity(), counting()));
    map.put("UNKNOWN", tasks.size() - map.values().stream().mapToLong(Long::valueOf).sum());
    return map;
  }

  protected String saferGet(Future<String> future) {
    if (future.isDone()) {
      try {
        return future.get(1, TimeUnit.MILLISECONDS);
      } catch (ExecutionException | TimeoutException e) {
        // Do nothing. get() is only called if the Future is done, so this shouldn't happen.

      } catch (InterruptedException e) {
        // Worst case scenario, we don't get the log message for this go around. I think.
        Thread.currentThread().interrupt();
      }
    }
    return null;
  }
}
