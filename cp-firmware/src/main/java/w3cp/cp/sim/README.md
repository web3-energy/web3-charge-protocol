# W3CP Charge Point Simulator API

This document describes the **W3CP Charge Point Simulator** — a lightweight, IEC 61851–aware virtual charge point used to drive the W3CP firmware/domain via events.

The simulator acts like **hardware**:

- It keeps its **own internal state**.
- It **emits events** into the connector / metering feeders.
- It never uses domain state as source of truth.

Base path:

    /api/sim/...

---

## 1. Concepts & Model

The simulator models:

- **Control Pilot (CP)**
    - Voltage (V)
    - PWM duty cycle (%)
- **Mechanical elements**
    - Relay / contactor
    - Connector lock
- **Connector state**
    - plugged / unplugged
    - charging / not charging
    - faulted / healthy
- **EV model**
    - `evMaxCurrentA`
    - `evSocPercent`
    - `evPhases`
- **Metering**
    - Instantaneous power
    - Total energy (Wh)

### IEC 61851 mapping (simplified)

- **State A** – 12 V → no EV, unplugged
- **State B** – 9 V → EV present, not charging
- **State C** – 6 V + PWM ≥ 10 % → EV present, charging
- **State F** – −12 V → fault

Additional rules:

- Relay can close only when:
    - plugged
    - not faulted
    - charging allowed by CP/PWM
- Lock is only engaged while charging; it unlocks when not charging or faulted.

A 1 Hz tick simulates:

- Energy accumulation in Wh
- SoC increase based on delivered energy
- Periodic metering events

---

## 2. High-Level Actions (human-level control)

These endpoints provide a clean “user flow” API without touching low-level CP/PWM directly.

### POST /api/sim/connector/actions/plug

Simulates plugging in an EV.

Effects (in the simulator):

- `plugged = true`
- `cpVoltage = 9 V` (State B)
- `lockState = true`
- `charging = false`
- relay open, no power

### POST /api/sim/connector/actions/unplug

Simulates unplugging the EV.

Effects:

- `plugged = false`
- `charging = false`
- `relayState = false`
- `lockState = false`
- `cpVoltage = 12 V` (State A)

### POST /api/sim/connector/actions/start-charging

Starts charging if allowed.

Preconditions:

- `plugged = true`
- `evSocPercent < 100 %`
- PWM allows current (duty ≥ 10 %)

Effects:

- `charging = true`
- `relayState = true`
- `lockState = true`
- `cpVoltage = 6 V` (State C)
- if PWM < 10 % it is bumped to a safe default (e.g. 50 %)

### POST /api/sim/connector/actions/stop-charging

Stops charging and returns to “plugged, not charging”.

Effects:

- `charging = false`
- `relayState = false`
- `lockState = false`
- `cpVoltage = 9 V` (if still plugged) or 12 V (if unplugged)

### POST /api/sim/connector/actions/fault

Forces the connector into a fault state.

Effects:

- `faulted = true`
- `charging = false`
- `relayState = false`
- `lockState = false`
- `cpVoltage = -12 V` (State F)

### POST /api/sim/connector/actions/clear-fault

Clears the fault and returns to the correct non-fault state based on plug.

Effects:

- `faulted = false`
- `charging = false`
- `cpVoltage = 9 V` (if plugged) or 12 V (if unplugged)
- relay remains open

---

## 3. Low-Level Hardware Control

These endpoints let you “drive the hardware” directly. They are mainly for advanced testing and debugging.

### POST /api/sim/connector/low-level/cp

Body:

    {
      "cpVoltage": 9.0,
      "pwmDutyCycle": 50.0
    }

Directly sets CP voltage and PWM duty cycle.

The simulator then **derives state** from this:

- `cpVoltage > 10 V` → unplugged, not charging
- `cpVoltage > 7.5 V` → plugged, not charging (B)
- `cpVoltage > 4.5 V` and `pwmDutyCycle ≥ 10 %` → plugged, charging (C)
- if in “C” but `pwmDutyCycle < 10 %`, charging is disabled

### POST /api/sim/connector/low-level/lock

Body:

    { "value": true }

Attempts to lock/unlock the connector.

Rules:

- If not plugged, lock is forced to `false`.
- Otherwise `lockState` is set to the requested value, but may still be overridden by fault logic.

### POST /api/sim/connector/low-level/relay

Body:

    { "value": true }

Attempts to open/close the relay.

Rules:

- If unplugged or faulted or not in charging mode, the relay is forced to `false`.
- Otherwise it follows the requested value.

### POST /api/sim/connector/low-level/pwm

Body:

    { "dutyCycle": 50.0 }

Sets the PWM duty cycle directly. The simulator clamps it into `[0, 100]`.

PWM affects the allowed current and therefore the charging power.

---

## 4. EV Simulation Configuration

The simulator includes a minimal EV model. You can configure it via:

### POST /api/sim/config/ev

Body (all fields optional):

    {
      "maxCurrentA": 32.0,
      "socPercent": 70.0,
      "phases": 3
    }

- `maxCurrentA` – maximum current the EV will ever draw
- `socPercent` – starting SoC (0–100 %)
- `phases` – 1 to 3 phases

These parameters affect:

- Measured charging power
- SoC progression during charging
- Per-phase current values in metering events

---

## 5. Metering API

### GET /api/sim/metering/status

Returns the domain `EnergyStatus` object as seen by the metering feeder.

### POST /api/sim/metering/reset

Resets the internal Wh counter:

- `totalEnergyWh = 0`
- emits a fresh `EnergyMeterUpdateEvent(0.0, 0.0)`

### Energy progression

Every second (tick):

- Power is recomputed from the current state:
    - `maxCurrentFromPwm = (pwm - 10 %) * 0.6` (for PWM ≥ 10 %)
    - `actualCurrent = min(maxCurrentFromPwm, evMaxCurrentA)`
    - currents below 6 A are treated as 0
    - `power = 230 V * actualCurrent * evPhases`
- Energy increases:
    - `totalEnergyWh += power / 3600.0`
- SoC increases based on delivered energy until it reaches 100 %.
- A new `InstantaneousPowerEvent` and `EnergyMeterUpdateEvent` is emitted.

---

## 6. Connector Status (Domain View)

### GET /api/sim/connector/status

Returns the domain `Connector` object created and maintained by `ConnectorSubFeeder`.

Use this to see **what the firmware/domain thinks** the connector state is, as opposed to the simulator’s raw state.

---

## 7. Simulator State (Internal View)

### GET /api/sim/state

Returns the simulator’s raw internal state:

Example response:

    {
      "plugged": true,
      "charging": false,
      "faulted": false,
      "relayState": false,
      "cpVoltage": 9.0,
      "pwmDutyCycle": 0.0,
      "totalEnergyWh": 420.5,
      "evMaxCurrentA": 32.0,
      "evSocPercent": 67.0,
      "evPhases": 3
    }

This is useful for debugging and visualizing what the simulator is doing internally.

---

## 8. Internal Behaviour Summary

### CP state machine

The CP logic runs whenever CP or PWM is changed or when states change:

- `cpVoltage > 10 V`
    - unplugged
    - charging = false
    - lock = false

- `7.5 V < cpVoltage ≤ 10 V`
    - plugged = true
    - charging = false

- `4.5 V < cpVoltage ≤ 7.5 V`
    - if `pwmDutyCycle ≥ 10 %` → plugged = true, charging = true
    - else → plugged = true, charging = false

- `cpVoltage < 0`
    - faulted = true
    - charging = false
    - relay = false
    - lock = false

### Relay

Relay can only be closed if:

- plugged = true
- charging = true
- faulted = false

Otherwise it is forced open.

### Lock

Lock rules:

- Only engaged while charging in State C.
- Automatically unlocked if:
    - unplugged
    - not charging
    - faulted

### Event emission order

Every state change calls `emitState()` which emits events in a fixed order:

1. `RelayStateChangedEvent`
2. `LockStateChangedEvent`
3. `ControlPilotSampleEvent`
4. `InstantaneousPowerEvent`
5. `EnergyMeterUpdateEvent`

The scheduled tick only emits metering events, not connector events.

---

## 9. Purpose

The W3CP Charge Point Simulator is designed to:

- Provide **deterministic, realistic** AC charging behaviour for development.
- Let you **test end-to-end flows** without any physical hardware.
- Exercise:
    - IEC 61851 CP/PWM behaviour
    - Relay and lock handling
    - Fault and recovery flows
    - Metering and energy accounting
    - EV SoC and current limits
- Back your frontend/UX demo with a “real” moving system.

Use the high-level endpoints for normal flows, and the low-level controls when you want to push the system into weird corners and validate robustness.
