package w3cp.model.status.charging;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class EvInfo {

  @Nullable private Identity identity;      // who/what is charging (good for UI)
  @Nullable private Energy energy;          // battery state (telemetry)
  @Nullable private EvProtocol protocol;    // how it talks to the CP
  @Nullable private Capabilities capabilities; // simple high-level flags

  // ───────────── Identity (UI + lookup) ─────────────

  @Getter
  @Setter
  public static class Identity {
    @Nullable private EvKind kind;       // car, boat, truck, ship...
    @Nullable private IdType idType;     // vin, fleetId, serialNumber, ...
    @Nullable private String id;         // raw identifier or "scheme:value"

    // nice for UX / fleet overviews:
    @Nullable private String brand;      // Tesla, Torqeedo, etc.
    @Nullable private String model;      // Model 3, X Shore 1, ...
    @Nullable private String label;      // "Ferry #3", "Forklift 2"
  }

  public enum EvKind {
    car,
    motorcycle,
    scooter,
    bus,
    truck,
    boat,
    ship,
    other
  }

  public enum IdType {
    vin,
    fleetId,
    serialNumber,
    imoNumber,
    other
  }

  // ───────────── Energy / Battery ─────────────

  @Getter
  @Setter
  public static class Energy {
    @Nullable private Integer soc;              // 0–100; null = not reported
    @Nullable private Integer socTarget;        // optional target SOC
    @Nullable private Double energyWh;          // current charge; null = not reported
    @Nullable private Double capacityWh;        // nominal capacity; null = not reported
  }

  public enum EvProtocol {
    iso15118,
    din70121,
    iec61851,
    unknown
  }

  // ───────────── High-level capabilities ─────────────

  @Getter
  @Setter
  public static class Capabilities {
    @Nullable private Boolean canDischarge;     // V2G / V2H capable; null = not reported
    @Nullable private Boolean hasMultiplePacks; // ships/buses/forklifts, etc.; null = not reported
    @Nullable private Boolean isFleetAsset;     // helps fleet filters / policies; null = not reported
  }
}