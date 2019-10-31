package poller;

import com.dyngr.PollerBuilder;
import com.dyngr.core.AttemptMaker;
import com.dyngr.core.AttemptResult;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.WaitStrategies;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// TODO Create nested Builder class

@Component
@Slf4j
public class PollingService {
  ExecutorService executorService;
  RestTemplate restTemplate;

  PollingService() {
    restTemplate = new RestTemplate();
    executorService = Executors.newFixedThreadPool(128);
  }

  //  public Future<String> pollForStatus(String jobId) {
  //    return newPollerBuilder().polling(attempt(jobId)).build().start();
  //  }

  public <T> Future<T> poll(Function<RestTemplate, AttemptResult<T>> supplierFunction) {

    return newPollerBuilder()
        .polling((AttemptMaker<T>) supplierFunction.apply(restTemplate))
        .build()
        .start();
  }

  @PreDestroy
  void destroy() {
    new StopExecutor().stop(executorService);
  }

  PollerBuilder newPollerBuilder() {
    return PollerBuilder.newBuilder()
        .withExecutorService(executorService)
        .stopIfException(false)
        .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(100));
  }
}
