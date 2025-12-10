package w3cp.cp.sim.api;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import w3cp.cp.logic.status.feeders.chargeport.one.ConnectorSubFeeder;
import w3cp.cp.logic.status.feeders.chargeport.one.MeteringSubFeeder;
import w3cp.cp.sim.SimulatorService;
import w3cp.model.status.charging.Connector;
import w3cp.model.status.charging.EnergyStatus;

@Path("/api/sim")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimulatorResource {

  @Inject
  SimulatorService simulator;

  @Inject
  ConnectorSubFeeder connectorFeeder;

  @Inject
  MeteringSubFeeder meteringFeeder;

  @Inject
  w3cp.cp.logic.status.feeders.chargeport.one.EvInfoSubFeeder evInfoFeeder;

  @POST
  @Path("/connector/actions/plug")
  public Uni<Response> plug() {
    return Uni.createFrom().item(() -> {
      simulator.plugVehicle();
      return Response.noContent().build();
    });
  }

  @POST
  @Path("/connector/actions/unplug")
  public Uni<Response> unplug() {
    return Uni.createFrom().item(() -> {
      simulator.unplugVehicle();
      evInfoFeeder.reset();
      return Response.noContent().build();
    });
  }

  public static final class StartChargingRequest {
    public w3cp.model.status.charging.EvInfo evInfo;
  }

  @POST
  @Path("/connector/actions/start-charging")
  public Uni<Response> startCharging(StartChargingRequest req) {
    return Uni.createFrom().item(() -> {
      if (req != null && req.evInfo != null) {
        evInfoFeeder.setEvInfo(req.evInfo);
        // Set battery from energyWh, capacityWh, and socTarget
        if (req.evInfo.getEnergy() != null) {
          if (req.evInfo.getEnergy().getCapacityWh() != null) {
            simulator.setEvCapacity(req.evInfo.getEnergy().getCapacityWh());
          }
          if (req.evInfo.getEnergy().getEnergyWh() != null) {
            simulator.setEvEnergy(req.evInfo.getEnergy().getEnergyWh());
          }
          if (req.evInfo.getEnergy().getSocTarget() != null) {
            simulator.setEvTargetSoc(req.evInfo.getEnergy().getSocTarget());
          }
        }
      }
      boolean success = simulator.startCharging();
      return success ? Response.noContent().build() : Response.status(400).build();
    });
  }

  @POST
  @Path("/connector/actions/stop-charging")
  public Uni<Response> stopCharging() {
    return Uni.createFrom().item(() -> {
      simulator.stopCharging();
      return Response.noContent().build();
    });
  }

  @POST
  @Path("/connector/actions/fault")
  public Uni<Response> fault() {
    return Uni.createFrom().item(() -> {
      simulator.triggerFault();
      return Response.noContent().build();
    });
  }

  @POST
  @Path("/connector/actions/clear-fault")
  public Uni<Response> clearFault() {
    return Uni.createFrom().item(() -> {
      simulator.clearFault();
      return Response.noContent().build();
    });
  }

  public static final class CpRequest {
    public double cpVoltage;
    public double pwmDutyCycle;
  }

  @POST
  @Path("/connector/low-level/cp")
  public Uni<Response> setCp(CpRequest req) {
    return Uni.createFrom().item(() -> {
      simulator.setControlPilot(req.cpVoltage, req.pwmDutyCycle);
      return Response.noContent().build();
    });
  }

  @POST
  @Path("/connector/low-level/lock")
  public Uni<Response> lock() {
    return Uni.createFrom().item(() -> {
      simulator.lockConnector();
      return Response.noContent().build();
    });
  }

  @POST
  @Path("/connector/low-level/unlock")
  public Uni<Response> unlock() {
    return Uni.createFrom().item(() -> {
      simulator.unlockConnector();
      return Response.noContent().build();
    });
  }

  @POST
  @Path("/connector/low-level/relay/close")
  public Uni<Response> closeRelay() {
    return Uni.createFrom().item(() -> {
      simulator.closeRelay();
      return Response.noContent().build();
    });
  }

  @POST
  @Path("/connector/low-level/relay/open")
  public Uni<Response> openRelay() {
    return Uni.createFrom().item(() -> {
      simulator.openRelay();
      return Response.noContent().build();
    });
  }

  public static final class SimStateDto {
    public boolean plugged;
    public boolean charging;
    public boolean faulted;
    public boolean relayState;
    public double cpVoltage;
    public double pwmDutyCycle;
    public double totalEnergyWh;
    public double evMaxCurrentA;
    public double evSocPercent;
    public int evPhases;
  }

  @GET
  @Path("/state")
  public Uni<SimStateDto> getSimState() {
    return Uni.createFrom().item(() -> {
      SimStateDto dto = new SimStateDto();
      dto.plugged = simulator.isPlugged();
      dto.charging = simulator.isCharging();
      dto.faulted = simulator.isFaulted();
      dto.relayState = simulator.isRelayState();
      dto.cpVoltage = simulator.getCpVoltage();
      dto.pwmDutyCycle = simulator.getPwmDutyCycle();
      dto.totalEnergyWh = simulator.getTotalEnergyWh();
      dto.evMaxCurrentA = simulator.getEvMaxCurrentA();
      dto.evSocPercent = simulator.getEvSocPercent();
      dto.evPhases = simulator.getEvPhases();
      return dto;
    });
  }

  @GET
  @Path("/connector/status")
  public Uni<Connector> getConnectorStatus() {
    return connectorFeeder.fetch();
  }

  @GET
  @Path("/metering/status")
  public Uni<EnergyStatus> getMeteringStatus() {
    return meteringFeeder.fetch();
  }

  public static final class EvConfigRequest {
    public Double maxCurrentA;
    public Double socPercent;
    public Integer phases;
  }

  @POST
  @Path("/config/ev")
  public Uni<Response> configureEv(EvConfigRequest req) {
    return Uni.createFrom().item(() -> {
      if (req.maxCurrentA != null) simulator.setEvMaxCurrent(req.maxCurrentA);
      if (req.socPercent != null) simulator.setEvSoc(req.socPercent);
      if (req.phases != null) simulator.setEvPhases(req.phases);
      return Response.noContent().build();
    });
  }

  public static final class PwmRequest {
    public double dutyCycle;
  }

  @POST
  @Path("/connector/low-level/pwm")
  public Uni<Response> setPwm(PwmRequest req) {
    return Uni.createFrom().item(() -> {
      simulator.setPwmDutyCycle(req.dutyCycle);
      return Response.noContent().build();
    });
  }

  @POST
  @Path("/metering/reset")
  public Uni<Response> resetEnergy() {
    return Uni.createFrom().item(() -> {
      simulator.resetEnergy();
      return Response.noContent().build();
    });
  }
}
