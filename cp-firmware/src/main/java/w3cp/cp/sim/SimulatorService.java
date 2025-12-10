package w3cp.cp.sim;

import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.logic.status.feeders.chargeport.one.ConnectorSubFeeder;
import w3cp.cp.logic.status.feeders.chargeport.one.MeteringSubFeeder;
import w3cp.cp.logic.status.feeders.chargeport.one.connector.*;
import w3cp.cp.logic.status.feeders.chargeport.one.metering.EnergyMeterUpdateEvent;
import w3cp.cp.logic.status.feeders.chargeport.one.metering.InstantaneousPowerEvent;

@Slf4j
@ApplicationScoped
public class SimulatorService {

  @Inject
  ConnectorSubFeeder connectorFeeder;

  @Inject
  MeteringSubFeeder meteringFeeder;

  @Inject
  w3cp.cp.logic.status.feeders.chargeport.one.EvInfoSubFeeder evInfoFeeder;

  @Getter
  private boolean plugged;
  @Getter
  private boolean charging;
  @Getter
  private boolean faulted;
  @Getter
  private boolean relayState;
  @Getter
  private boolean lockState;
  @Getter
  private double cpVoltage = 12.0;
  @Getter
  private double pwmDutyCycle = 0.0;
  @Getter
  private double totalEnergyWh = 0.0;
  
  @Getter
  private double evMaxCurrentA = 32.0;
  @Getter
  private double evSocPercent = 50.0;
  @Getter
  private double evTargetSocPercent = 100.0;
  @Getter
  private int evPhases = 1;
  @Getter
  private double evEnergyWh = 0.0;
  @Getter
  private double evCapacityWh = 50000.0; // 50 kWh default
  private static final double VOLTAGE = 230.0;
  private double lastPowerW = 0.0;
  private final java.util.Random random = new java.util.Random();

  @PostConstruct
  void init() {
    emitState();
  }

  private double pwmToMaxCurrent(double pwmPercent) {
    if (pwmPercent < 10.0) return 0.0;
    return (pwmPercent - 10.0) * 0.6;
  }

  private double calculateChargingPower() {
    if (!relayState || !charging) return 0.0;
    
    // Stop if reached target or 100%
    if (evSocPercent >= evTargetSocPercent || evSocPercent >= 100.0) {
      return 0.0;
    }
    
    // 1) Max power from hardware
    double maxCurrentFromPwm = pwmToMaxCurrent(pwmDutyCycle);
    double actualCurrent = Math.min(maxCurrentFromPwm, evMaxCurrentA);
    if (actualCurrent < 6.0) return 0.0;
    double maxPowerW = VOLTAGE * actualCurrent * evPhases;
    
    // 2) SoC-based tapering: full power below 80%, then ramp down
    double socFactor;
    if (evSocPercent < 80.0) {
      socFactor = 1.0;
    } else if (evSocPercent >= 100.0) {
      socFactor = 0.0;
    } else {
      // Linear taper from 80% → 100% down to 30% of max
      double t = (evSocPercent - 80.0) / 20.0;
      socFactor = 1.0 - 0.7 * t;
    }
    
    // 3) Random small fluctuation ±5%
    double noise = 1.0 + (random.nextDouble() * 0.1 - 0.05);
    
    double power = maxPowerW * socFactor * noise;
    
    // 4) Clamp to [0, maxPowerW]
    return Math.max(0.0, Math.min(power, maxPowerW));
  }

  private void emitState() {
    if (faulted) relayState = false;
    if (!plugged || faulted || !charging) lockState = false;
    if (!relayState && cpVoltage < 7.5 && cpVoltage > 4.5) {
      cpVoltage = plugged ? 9.0 : 12.0;
    }
    
    connectorFeeder.applyEvent(new RelayStateChangedEvent(relayState));
    connectorFeeder.applyEvent(new LockStateChangedEvent(lockState));
    connectorFeeder.applyEvent(new ControlPilotSampleEvent(cpVoltage, pwmDutyCycle));
  }

  @Scheduled(every = "1s")
  void tick() {
    double targetPower = calculateChargingPower();
    
    // Smooth power changes (low-pass filter)
    double alpha = 0.3;
    double power = lastPowerW + alpha * (targetPower - lastPowerW);
    lastPowerW = power;
    
    if (power > 0) {
      totalEnergyWh += power / 3600.0;
      
      // Update battery energy
      evEnergyWh += power / 3600.0;
      
      // Calculate SoC from actual energy and capacity
      if (evCapacityWh > 0) {
        double newSoc = (evEnergyWh / evCapacityWh) * 100.0;
        evSocPercent = Math.min(Math.min(100.0, evTargetSocPercent), newSoc);
      }
      
      // Update EV info with new SoC
      updateSoc();
      
      // Stop charging if reached target
      if (evSocPercent >= evTargetSocPercent || evSocPercent >= 100.0) {
        log.info("Charging complete: SoC {:.1f}% (target: {:.1f}%)", evSocPercent, evTargetSocPercent);
        stopCharging();
      }
    }
    
    double currentPerPhase = power > 0 ? power / (VOLTAGE * evPhases) : 0.0;
    java.util.List<Double> currents = java.util.Collections.nCopies(evPhases, currentPerPhase);
    meteringFeeder.applyEvent(new InstantaneousPowerEvent(relayState ? VOLTAGE : 0.0, currents, power, 0.0));
    meteringFeeder.applyEvent(new EnergyMeterUpdateEvent(totalEnergyWh, 0.0));
  }

  public synchronized void plugVehicle() {
    plugged = true;
    faulted = false;
    lockState = false;
    cpVoltage = 9.0;
    pwmDutyCycle = 0.0;
    emitState();
  }

  public synchronized void unplugVehicle() {
    plugged = false;
    charging = false;
    faulted = false;
    relayState = false;
    lockState = false;
    cpVoltage = 12.0;
    pwmDutyCycle = 0.0;
    emitState();
  }

  public synchronized boolean startCharging() {
    if (charging) {
      log.warn("startCharging rejected: already charging");
      return false;
    }
    log.info("startCharging: initiating charge session");
    plugged = true;
    charging = true;
    faulted = false;
    relayState = true;
    lockState = true;
    cpVoltage = 6.0;
    pwmDutyCycle = 100.0;
    emitState();
    return true;
  }

  public synchronized void stopCharging() {
    charging = false;
    relayState = false;
    lockState = false;
    cpVoltage = plugged ? 9.0 : 12.0;
    pwmDutyCycle = 0.0;
    emitState();
  }

  public synchronized void triggerFault() {
    faulted = true;
    charging = false;
    relayState = false;
    cpVoltage = -12.0;
    pwmDutyCycle = 0.0;
    emitState();
  }

  public synchronized void clearFault() {
    faulted = false;
    charging = false;
    cpVoltage = plugged ? 9.0 : 12.0;
    pwmDutyCycle = 0.0;
    emitState();
  }

  public synchronized void setControlPilot(double cpVoltage, double pwmDutyCycle) {
    this.cpVoltage = Math.max(-15.0, Math.min(15.0, cpVoltage));
    this.pwmDutyCycle = Math.max(0.0, Math.min(100.0, pwmDutyCycle));
    
    if (this.cpVoltage > 10.0) {
      plugged = false;
      charging = false;
      lockState = false;
    } else if (this.cpVoltage > 7.5) {
      plugged = true;
      charging = false;
      lockState = false;
    } else if (this.cpVoltage > 4.5) {
      if (this.pwmDutyCycle < 10.0) {
        charging = false;
        lockState = false;
      } else {
        plugged = true;
        charging = true;
      }
    }
    
    emitState();
  }

  public synchronized void lockConnector() {
    if (!plugged) return;
    this.lockState = true;
    emitState();
  }

  public synchronized void unlockConnector() {
    this.lockState = false;
    emitState();
  }

  public synchronized void closeRelay() {
    if (!plugged || faulted || !charging) return;
    relayState = true;
    emitState();
  }

  public synchronized void openRelay() {
    relayState = false;
    emitState();
  }

  public synchronized void setEvMaxCurrent(double amps) {
    this.evMaxCurrentA = Math.max(0.0, Math.min(80.0, amps));
  }

  public synchronized void setEvEnergy(double energyWh) {
    this.evEnergyWh = Math.max(0.0, energyWh);
    // Compute SoC from energy and capacity
    if (evCapacityWh > 0) {
      this.evSocPercent = Math.min(100.0, (evEnergyWh / evCapacityWh) * 100.0);
    }
    updateSoc();
  }

  public synchronized void setEvTargetSoc(double percent) {
    this.evTargetSocPercent = Math.max(0.0, Math.min(100.0, percent));
  }

  public synchronized void setEvCapacity(double capacityWh) {
    this.evCapacityWh = Math.max(1.0, capacityWh);
    // Recompute SoC with new capacity
    if (evCapacityWh > 0) {
      this.evSocPercent = Math.min(100.0, (evEnergyWh / evCapacityWh) * 100.0);
    }
  }

  public synchronized void setEvSoc(double percent) {
    // Deprecated: compute from energy/capacity instead
    this.evSocPercent = Math.max(0.0, Math.min(100.0, percent));
    this.evEnergyWh = (evSocPercent / 100.0) * evCapacityWh;
    updateSoc();
  }

  private void updateSoc() {
    evInfoFeeder.applyEvent(new w3cp.cp.logic.status.feeders.chargeport.one.evinfo.SocUpdateEvent(evSocPercent));
    evInfoFeeder.applyEvent(new w3cp.cp.logic.status.feeders.chargeport.one.evinfo.EnergyUpdateEvent(evEnergyWh));
  }

  public synchronized void setEvPhases(int phases) {
    this.evPhases = Math.max(1, Math.min(3, phases));
  }

  public synchronized void setPwmDutyCycle(double percent) {
    this.pwmDutyCycle = Math.max(0.0, Math.min(100.0, percent));
  }

  public synchronized void resetEnergy() {
    this.totalEnergyWh = 0.0;
    meteringFeeder.applyEvent(new EnergyMeterUpdateEvent(0.0, 0.0));
  }
}
