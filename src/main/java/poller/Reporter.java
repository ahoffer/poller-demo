package poller;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Reporter {

  private final List<Future<String>> tasks;
  private final int delayBeforeFirstReport;
  private ScheduledExecutorService reportingExecutor = Executors.newScheduledThreadPool(1);
  private int reportingInterval;

  public void stopReporting() {
    new StopExecutor().stop(reportingExecutor);
  }

  public Reporter(List<Future<String>> tasks) {
    this.tasks = tasks;
    reportingInterval = 3;
    delayBeforeFirstReport = 1;
  }

  public void startReporting() {
    reportingExecutor.scheduleAtFixedRate(
        this::report, delayBeforeFirstReport, reportingInterval, TimeUnit.SECONDS);
  }

  public void report() {
    TaskOutcomeCollector taskOutcomes = new TaskOutcomeCollector(tasks);
    if (taskOutcomes.numNotDone() > 0) {
      log.info("Local polling tasks {}", taskOutcomes.get());
    } else {
      log.info("All jobs are finished!");
      stopReporting();
    }
    log.info("Remote jobs {}", new JobOutcomeCollector(tasks).get());
  }
}
