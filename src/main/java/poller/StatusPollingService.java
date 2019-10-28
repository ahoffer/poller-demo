package poller;

import static com.dyngr.core.AttemptResults.finishWith;
import static com.dyngr.core.AttemptResults.justContinue;
import static poller.JobStatus.COMPLETED;
import static poller.JobStatus.FAILED;

import com.dyngr.PollerBuilder;
import com.dyngr.core.AttemptMaker;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.WaitStrategies;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class StatusPollingService {
  ExecutorService executorService;
  RestTemplate restTemplate;

  StatusPollingService() {
    restTemplate = new RestTemplate();
    executorService = Executors.newFixedThreadPool(128);
  }

  public Future<String> pollForStatus(String jobId) {
    return newPollerBuilder().polling(attempt(jobId)).build().start();
  }

  @PreDestroy
  void destroy() {
    new StopExecutor().stop(executorService);
  }

  AttemptMaker<String> attempt(String jobId) {
    return () -> {
      ResponseEntity<String> entity =
          restTemplate.getForEntity("http://localhost:8080/{jobId}", String.class, jobId);
      HttpStatus reponseStatus = entity.getStatusCode();
      String jobStatus = entity.getBody();
      log.info("Job={} ResponseStatus={} JobStatus={}", jobId, reponseStatus.value(), jobStatus);
      if (COMPLETED.name().equals(jobStatus) || FAILED.name().equals(jobStatus)) {
//        log.info("Polling stopped for job {}", jobId);
        return finishWith(jobStatus);
      }
      return justContinue();
    };
  }

  PollerBuilder newPollerBuilder() {
    return PollerBuilder.newBuilder()
        .withExecutorService(executorService)
        .stopIfException(false)
        .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(100));
  }
}
