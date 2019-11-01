package poller;

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
import org.springframework.stereotype.Component;

// TODO Create nested Builder class

@Component
@Slf4j
public class PollingService {

  private final int corePoolSize;
  ExecutorService executorService;
  private int giveUpAfter;

  public PollingService() {
    super();
    corePoolSize = 128;
    executorService = Executors.newFixedThreadPool(corePoolSize);
    giveUpAfter = 30;
  }

  public <T> Future<T> poll(AttemptMaker<T> pollingTask)  {

    try {
      return newPollerBuilder()
          .polling(pollingTask)
          .build()
          .start();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
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
        .withStopStrategy(StopStrategies.stopAfterDelay(giveUpAfter, TimeUnit.SECONDS));
  }
}
