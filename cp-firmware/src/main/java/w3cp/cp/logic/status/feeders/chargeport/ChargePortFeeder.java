package w3cp.cp.logic.status.feeders.chargeport;

import io.smallrye.mutiny.Uni;

public interface ChargePortFeeder<T> {
  Uni<T> fetch();
}
