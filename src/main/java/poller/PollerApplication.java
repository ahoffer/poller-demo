package poller;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PollerApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(PollerApplication.class)
        .logStartupInfo(false)
        .bannerMode(Mode.OFF)
        .run(args);
  }
}
