package polling.service;

import static com.dyngr.core.AttemptResults.continueFor;
import static com.dyngr.core.AttemptResults.finishWith;
import static com.dyngr.core.AttemptResults.justContinue;

import com.dyngr.core.AttemptMaker;
import com.dyngr.core.AttemptResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/** Class to encapsulate polling library and the things that should happend as part of polling. */
@Slf4j
public class PollingTask<T> implements AttemptMaker<T> {

  private final RestTemplate restTemplate;
  private final String jobId;

  public PollingTask(String jobId, RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    this.jobId = jobId;
  }

  /**
   * Here are the guts of the polling job.
   * @return
   */
  @Override
  public AttemptResult<T> process() {
    HttpStatus reponseStatus = null;
    String jobStatus = "UNKNOWN";
    String uriTemplate = "http://localhost:9500/job/{jobId}";
    try {
      ResponseEntity<String> entity = restTemplate.getForEntity(uriTemplate, String.class, jobId);
      reponseStatus = entity.getStatusCode();
      jobStatus = entity.getBody();
    } catch (HttpStatusCodeException e) {

      reponseStatus = e.getStatusCode();

    } catch (ResourceAccessException e) {
      resourceAccessException(uriTemplate, e);
    }

    logResultOfTheAttempt(reponseStatus, jobStatus);

    // If job is done, stop polling
    if (isJobComplete(jobStatus)) {

      // Stop polling
      return (AttemptResult<T>) finishWith(jobStatus);
    }

    // Job not done, keep polling
    return justContinue();
  }

  private boolean isJobComplete(String jobStatus) {
    return "COMPLETED".equals(jobStatus) || "FAILED".equals(jobStatus);
  }

  private void logResultOfTheAttempt(HttpStatus reponseStatus, String jobStatus) {
    log.info("Job={} ResponseStatus={} JobStatus={}", jobId, reponseStatus.value(), jobStatus);
  }

  private void resourceAccessException(String uriTemplate, ResourceAccessException e) {
    log.info("Could not connect to {}", uriTemplate);

    // Exit lambda, but keep polling
    continueFor(e);
  }
}
