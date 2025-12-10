package w3cp.cp.logic.status.feeders;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.logic.events.ConnectionOpenedEvent;

import java.time.Instant;

@Slf4j
@ApplicationScoped
public class OnlineSinceFeeder implements Feeder<Instant> {

  private Instant onlineSince;

  @PostConstruct
  void init() {
    this.onlineSince = Instant.now();
  }

  @ConsumeEvent("CONNECTION_OPENED")
  void onConnectionOpened(ConnectionOpenedEvent event) {
    this.onlineSince = Instant.now();
  }

  @Override
  public Uni<Instant> fetch() {
    return Uni.createFrom().item(onlineSince);
  }
}
