package poller;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StopExecutor {

  public List<Runnable> stop(@Nullable ExecutorService executorService) {
    if (executorService == null) {
      return Collections.emptyList();
    }
    List<Runnable> unfinished = Collections.emptyList();
    if (executorService != null) {
      executorService.shutdown();
      try {
        if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
          unfinished = executorService.shutdownNow();
        }
      } catch (InterruptedException e) {
        unfinished = executorService.shutdownNow();
      }
    }
    log.info("Stopped excutor service with {} tasks uncompleted", unfinished.size());
    return unfinished;
  }
}
