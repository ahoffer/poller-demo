package poller;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class JobReport {

  private final List<Future<String>> jobStatuses;
  private ScheduledExecutorService reportingExecutor = Executors.newScheduledThreadPool(1);
  private int reportingInterval;

  public void stopReporting() {
    new StopExecutor().stop(reportingExecutor);
  }

  public JobReport(List<Future<String>> jobStatuses) {
    // Must be a live view of the list that can grow over time.
    this.jobStatuses = jobStatuses;
     reportingInterval = 10;
  }

   public void startReporting() {
     reportingExecutor.scheduleAtFixedRate(report(), 1, reportingInterval, TimeUnit.SECONDS);
  }



  public Runnable report() {
    return () -> {
      long finished = jobStatuses.stream().filter(Future::isDone).count();
      long cancelled = jobStatuses.stream().filter(Future::isCancelled).count();
      long notDone = jobStatuses.size() - finished - cancelled;
      if (notDone > 0) {
        log.info(
            "Summary {} jobs are finished, {} jobs are cancelled, {} jobs are not done",
            finished,
            cancelled,
            notDone);
      } else {
        log.info("All jobs are finished!");
        stopReporting();
      }
    };
  }
}
