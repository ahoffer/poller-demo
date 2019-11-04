package polling.interfaces;

import com.dyngr.core.AttemptMaker;
import java.util.concurrent.Future;

public interface PollingService {

  <T> Future<T> poll(AttemptMaker<T> pollingTask);
}
