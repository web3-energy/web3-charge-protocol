package w3cp.cp.logic.status.feeders.chargeport.one;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.logic.status.feeders.Feeder;
import w3cp.model.status.charging.ChargePort;

@Slf4j
@ApplicationScoped
public class ChargePortOrchestratorFeeder implements Feeder<ChargePort> {

  @Inject
  ConnectorSubFeeder connectorFeeder;
  
  @Inject
  MeteringSubFeeder meteringFeeder;
  
  @Inject
  SessionSubFeeder sessionFeeder;
  
  @Inject
  PortThermalInfoSubFeeder portThermalInfoFeeder;
  
  @Inject
  EvInfoSubFeeder evInfoFeeder;

  @Override
  public Uni<ChargePort> fetch() {
    return Uni.combine().all().unis(
        connectorFeeder.fetch(),
        meteringFeeder.fetch(),
        sessionFeeder.fetch(),
        portThermalInfoFeeder.fetch(),
        evInfoFeeder.fetch()
    ).asTuple().map(tuple -> {
      ChargePort chargePort = new ChargePort();
      chargePort.setChargePortId(1);
      chargePort.setConnector(tuple.getItem1());
      chargePort.setMetering(tuple.getItem2());
      chargePort.setSession(tuple.getItem3());
      chargePort.setThermalInfo(tuple.getItem4());
      chargePort.setEvInfo(tuple.getItem5());
      return chargePort;
    });
  }
}
