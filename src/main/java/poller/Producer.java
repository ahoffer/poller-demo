package poller;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class Producer {
  PollingService pollingService;
  int numberOfSimulatedJobs;
  List<Future<String>> statusFutures;
  Reporter reporter;
  RestTemplate restTemplate;

  @PostConstruct
  void go() {
    resetSimulatedEndpoint();
    start();
  }

  private void resetSimulatedEndpoint() {
    restTemplate.delete("http://localhost:9500/job");
  }

  public Producer() {
    this.pollingService = new PollingService();
    restTemplate = new RestTemplate();
    numberOfSimulatedJobs = 100;
  }

  void start() {
    startSimulation();
    startReporting();
  }

  private void startReporting() {
    reporter = new Reporter(statusFutures);
    reporter.startReporting();
  }

  private void startSimulation() {
    log.info("STARTING SIMULATION");
    statusFutures =
        IntStream.rangeClosed(1, numberOfSimulatedJobs)
            .mapToObj(jobNum -> String.format("%03d", jobNum))
            .map(j -> new PollingTask<String>(j, restTemplate))
            .map(pollingService::poll)
            .collect(Collectors.toList());
  }

  @PreDestroy
  void destroy() {
    log.info("ENDING SIMULATION");
    if (reporter != null) {
      reporter.stopReporting();
    }
  }
}
