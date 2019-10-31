package poller;

import static com.dyngr.core.AttemptResults.finishWith;
import static com.dyngr.core.AttemptResults.justContinue;
import com.dyngr.PollerBuilder;
import com.dyngr.core.AttemptMaker;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.WaitStrategies;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

//TODO Create nested Builder class

@Component
@Slf4j
public class PollingService {
  ExecutorService executorService;
  RestTemplate restTemplate;

  PollingService() {
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
    AtomicReference<HttpStatus> reponseStatus = new AtomicReference<>();
    AtomicReference<String> jobStatusRef = new AtomicReference<>();
    return () -> {
      try {
        ResponseEntity<String> entity =
            restTemplate.getForEntity("http://localhost:9090/job/{jobId}", String.class, jobId);
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
