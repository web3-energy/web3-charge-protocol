package w3cp.cp.logic.status.feeders.chargeport.one.metering;

public interface MeteringEvent {
  void applyTo(MeteringRuntimeState state);
}
