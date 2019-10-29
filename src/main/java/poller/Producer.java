package poller;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class Producer {
  PollingService pollingService;
  int numberOfSimulatedJobs = 100;
  List<Future<String>> statusFutures;
  private ScheduledExecutorService reportingExecutor;

  @GetMapping("/start")
  void start() {
    pollingService = new PollingService();
    reportingExecutor = Executors.newScheduledThreadPool(1);
    startSimulation();
    startReporting();
  }

  private void startReporting() {
    reportingExecutor.scheduleAtFixedRate(reportingTask(), 0, 10, TimeUnit.SECONDS);
  }

  Runnable reportingTask() {
    return () -> {
      long finished = statusFutures.stream().filter(Future::isDone).count();
      long cancelled = statusFutures.stream().filter(Future::isCancelled).count();
      long notDone = numberOfSimulatedJobs - finished - cancelled;
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

  private void startSimulation() {
    log.info("STARTING SIMULATION");
    statusFutures =
        IntStream.rangeClosed(1, numberOfSimulatedJobs)
            .mapToObj(jobNum -> String.format("%03d", jobNum))
            .map(pollingService::pollForStatus)
            .collect(Collectors.toList());
  }

  @PreDestroy
  void destroy() {
    log.info("ENDING SIMULATION");
    stopReporting();
  }

  private void stopReporting() {
    new StopExecutor().stop(reportingExecutor);
  }
}
