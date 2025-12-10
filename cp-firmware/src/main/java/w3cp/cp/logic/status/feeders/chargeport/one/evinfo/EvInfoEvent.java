package w3cp.cp.logic.status.feeders.chargeport.one.evinfo;

public interface EvInfoEvent {
  void applyTo(EvInfoRuntimeState state);
}
