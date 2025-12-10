package w3cp.cp.logic.status.feeders;

import io.smallrye.mutiny.Uni;

public interface Feeder<T> {
  Uni<T> fetch();
}
