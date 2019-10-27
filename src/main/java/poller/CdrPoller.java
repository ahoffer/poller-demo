package poller;

import static com.dyngr.core.AttemptResults.*;

import com.dyngr.core.AttemptMaker;
import com.dyngr.core.AttemptResults;
import com.dyngr.core.DefaultPoller;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.WaitStrategies;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CdrPoller {

  RestTemplate restTemplate;
  private ScheduledExecutorService executorService;
  private DefaultPoller<Void> poller;

  @PreDestroy
  public void cleanUp() {
    log.info("SHUTTING DOWN POLLER");
  }

  @PostConstruct
  private void init() {
    executorService = Executors.newScheduledThreadPool(128);
    restTemplate = new RestTemplate();
    poller = createPoller();
    beginPolling();
  }

  private DefaultPoller<Void> createPoller() {
    return new DefaultPoller<>(
        action(),
        StopStrategies.stopAfterAttempt(100),
        WaitStrategies.fixedWait(1, TimeUnit.SECONDS),
        Executors.newFixedThreadPool(128));
  }

  private void beginPolling() {
    log.info("BEGIN POLLING");
    poller.start();
    log.info("END POLLING");
  }

  private AttemptMaker<Void> action() {
    return () -> {
      ResponseEntity<String> entity =
          restTemplate.getForEntity("http://localhost:8080/1", String.class);
      HttpStatus reponseStatus = entity.getStatusCode();
      String jobStatus = entity.getBody();
      //        return AttemptResults.finishWith(resp.getMessage());
      log.info("Response Status={} Job Status={}", reponseStatus.value(), jobStatus);
      return justContinue();
    };
  }
}
