package poller;

import com.dyngr.PollerBuilder;
import com.dyngr.core.AttemptMaker;
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

  private final int corePoolSize;
  ExecutorService executorService;
  RestTemplate restTemplate;

  public PollingService() {
    super();
    restTemplate = new RestTemplate();
    corePoolSize = 128;
    executorService = Executors.newFixedThreadPool(corePoolSize);
  }

  public <T> Future<T> poll(Function<RestTemplate, AttemptMaker<T>> supplierFunction) {

    return newPollerBuilder()
        .polling(supplierFunction.apply(restTemplate))
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
