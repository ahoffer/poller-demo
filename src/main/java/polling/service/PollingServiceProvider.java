package polling.service;

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
import org.springframework.stereotype.Service;
import polling.common.StopExecutor;
import polling.interfaces.PollingService;

/**
 * This is class is meant to be instantiated as a Bean, Component, or Service. The class's
 * responsibilities are to manage the settings for polling and kick off new polling tasks. Other
 * responsibilities include settings the wait and retry behavior. It also creates and configures the
 * executor, and ensures that it is shutdown properly.
 */
@Service
@Slf4j
public class PollingServiceProvider implements PollingService {

  private final int corePoolSize;
  ExecutorService executorService;
  private int giveUpAfter;

  public PollingServiceProvider() {
    super();
    corePoolSize = 128;
    executorService = Executors.newFixedThreadPool(corePoolSize);
    giveUpAfter = 20;
  }

  @Override
  public <T> Future<T> poll(AttemptMaker<T> pollingTask) {

    try {
      return builder().polling(pollingTask).build().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @PreDestroy
  void destroy() {
    new StopExecutor().stop(executorService);
  }

  PollerBuilder builder() {
    return PollerBuilder.newBuilder()
        .withExecutorService(executorService)
        .stopIfException(false)
        .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterDelay(giveUpAfter, TimeUnit.SECONDS));
  }
}
