package w3cp.cp.logic.status.feeders.chargeport.one;

import jakarta.enterprise.context.ApplicationScoped;
import w3cp.cp.logic.status.feeders.chargeport.ChargePortFeeder;
import w3cp.cp.logic.status.feeders.chargeport.one.evinfo.EvInfoRuntimeState;
import io.smallrye.mutiny.Uni;
import w3cp.model.status.charging.EvInfo;

@ApplicationScoped
public class EvInfoSubFeeder implements ChargePortFeeder<EvInfo> {

  private final EvInfoRuntimeState state = new EvInfoRuntimeState();

  public synchronized void setEvInfo(EvInfo evInfo) {
    state.setEvInfo(evInfo);
  }

  public synchronized void reset() {
    state.reset();
  }

  public synchronized void applyEvent(w3cp.cp.logic.status.feeders.chargeport.one.evinfo.EvInfoEvent event) {
    event.applyTo(state);
  }

  @Override
  public Uni<EvInfo> fetch() {
    return Uni.createFrom().item(() -> 
        state.hasEvInfo() ? state.toSnapshotDto() : null
    );
  }
}
