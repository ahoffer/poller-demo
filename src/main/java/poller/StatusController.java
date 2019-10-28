package poller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

  final Map<String, Job> jobs = new HashMap<>();
  JobFactory jobFactory = new JobFactory();

  @GetMapping(value = "/{jobId}")
  public String status(@PathVariable String jobId) {
    return jobs.computeIfAbsent(jobId, jobFactory::next).getCurrentStatus();
  }
}
