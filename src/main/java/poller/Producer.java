package poller;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Producer {
  StatusPollingService pollingService;
  int numberOfSimulatedJobs = 2;
  List<Future<String>> statusFutures;
  Timer reportingTimer;

  @PostConstruct
  void init() {
    pollingService = new StatusPollingService();
    reportingTimer = new Timer("Reporting", true);
//    startReportingProgress();
    startSimulation();
  }

  private void startReportingProgress() {
    reportingTimer.scheduleAtFixedRate(
        reportingTask(),
        0,
        10000);
  }

  private TimerTask reportingTask() {
    return new TimerTask() {
      @Override
      public void run() {
        long finished = statusFutures.stream().filter(Future::isDone).count();
        long cancelled = statusFutures.stream().filter(Future::isCancelled).count();
        long notDone = numberOfSimulatedJobs - finished - cancelled;
        log.info(
            "{} jobs are finished, {} jobs are cancelled, {} jobs are not done",
            finished,
            cancelled,
            notDone);
      }
    };
  }

  private void reportOnSimulation() {}

  private void startSimulation() {
    log.info("STARTING SIMULATION");
    statusFutures =
        IntStream.rangeClosed(1, numberOfSimulatedJobs)
            .mapToObj(String::valueOf)
            .map(pollingService::pollForStatus)
            .collect(Collectors.toList());
  }

  @PreDestroy
  void destroy() {
    log.info("ENDING SIMULATION");
  }
}
