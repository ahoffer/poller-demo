package poller;

import static com.dyngr.core.AttemptResults.finishWith;
import static com.dyngr.core.AttemptResults.justContinue;

import com.dyngr.core.AttemptMaker;
import com.dyngr.core.AttemptResult;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
public class Producer {
  PollingService pollingService;
  int numberOfSimulatedJobs = 100;
  List<Future<String>> statusFutures;
  JobReport jobReport;

  @PostConstruct
  void go() {
    start();
  }

  @GetMapping("/start")
  void start() {
    pollingService = new PollingService();
    startSimulation();
    startReporting();
  }

  private void startReporting() {
    jobReport = new JobReport(statusFutures);
    jobReport.startReporting();
  }

  private void startSimulation() {
    log.info("STARTING SIMULATION");
    statusFutures =
        IntStream.rangeClosed(1, numberOfSimulatedJobs)
            .mapToObj(jobNum -> String.format("%03d", jobNum))
            .map(this::attempt)
            .map(pollingService::poll)
            .collect(Collectors.toList());
  }

  // TODO Handle refused connectinx caused by java.net.ConnectException
  // TODO Handle jobs that never finish
  Function<RestTemplate, AttemptMaker<String>> attempt(String jobId) {
    return (restTemplate) -> () -> theFunctionToExecute(jobId, restTemplate);
  }

  private AttemptResult<String> theFunctionToExecute(String jobId, RestTemplate restTemplate) {
    AtomicReference<HttpStatus> reponseStatus = new AtomicReference<>();
    AtomicReference<String> jobStatusRef = new AtomicReference<>();
    try {
      ResponseEntity<String> entity =
          restTemplate.getForEntity("http://localhost:9000/job/{jobId}", String.class, jobId);
      reponseStatus.set(entity.getStatusCode());
      jobStatusRef.set(entity.getBody());
    } catch (HttpStatusCodeException e) {
      reponseStatus.set(e.getStatusCode());
      jobStatusRef.set("UNKNOWN");
    }
    String jobStatus = jobStatusRef.get();
    log.info(
        "Job={} ResponseStatus={} JobStatus={}", jobId, reponseStatus.get().value(), jobStatus);
    if ("COMPLETED".equals(jobStatus) || "FAILED".equals(jobStatus)) {
      return finishWith(jobStatus);
    }
    return justContinue();
  }

  @PreDestroy
  void destroy() {
    log.info("ENDING SIMULATION");
    if (jobReport != null) {
      jobReport.stopReporting();
    }
  }
}
