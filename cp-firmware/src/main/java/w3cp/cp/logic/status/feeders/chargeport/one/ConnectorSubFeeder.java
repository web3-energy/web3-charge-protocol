package w3cp.cp.logic.status.feeders.chargeport.one;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.logic.status.feeders.chargeport.ChargePortFeeder;
import w3cp.cp.logic.status.feeders.chargeport.one.connector.ConnectorEvent;
import w3cp.cp.logic.status.feeders.chargeport.one.connector.ConnectorRuntimeState;
import w3cp.cp.logic.status.feeders.chargeport.one.session.VehiclePluggedEvent;
import w3cp.cp.logic.status.feeders.chargeport.one.session.VehicleUnpluggedEvent;
import w3cp.model.status.charging.Connector;

import java.time.Instant;

@Slf4j
@ApplicationScoped
public class ConnectorSubFeeder implements ChargePortFeeder<Connector> {

  @Inject
  SessionSubFeeder sessionFeeder;

  private final ConnectorRuntimeState state = new ConnectorRuntimeState();
  private final Object lock = new Object();
  private Connector.ConnectorStatus previousStatus;

  @Override
  public Uni<Connector> fetch() {
    return Uni.createFrom().item(() -> {
      synchronized (lock) {
        return state.toSnapshotDto();
      }
    });
  }

  public void applyEvent(ConnectorEvent event) {
    synchronized (lock) {
      Connector.ConnectorStatus oldStatus = state.getStatus();
      event.applyTo(state);
      inferHighLevelStatus();
      Connector.ConnectorStatus newStatus = state.getStatus();
      
      detectSessionTransitions(oldStatus, newStatus);
    }
  }

  private void detectSessionTransitions(Connector.ConnectorStatus oldStatus, Connector.ConnectorStatus newStatus) {
    boolean wasPlugged = oldStatus != null && oldStatus != Connector.ConnectorStatus.available;
    boolean isPlugged = newStatus != null && newStatus != Connector.ConnectorStatus.available;
    
    if (!wasPlugged && isPlugged) {
      sessionFeeder.applyEvent(new VehiclePluggedEvent(Instant.now()));
    } else if (wasPlugged && !isPlugged) {
      sessionFeeder.applyEvent(new VehicleUnpluggedEvent(Instant.now()));
    }
  }

  private void inferHighLevelStatus() {
    Connector.Iec61851State iec = state.getIec61851State();
    Boolean relay = state.getRelayClosed();
    
    if (iec == null) {
      if (state.getStatus() == null) {
        state.setStatus(Connector.ConnectorStatus.unknown);
      }
      return;
    }
    
    switch (iec) {
      case a -> state.setStatus(Connector.ConnectorStatus.available);
      case b -> state.setStatus(Connector.ConnectorStatus.plugged);
      case c, d -> state.setStatus(Boolean.TRUE.equals(relay) ? 
          Connector.ConnectorStatus.charging : Connector.ConnectorStatus.plugged);
      case e, f -> state.setStatus(Connector.ConnectorStatus.faulted);
    }
  }


}
