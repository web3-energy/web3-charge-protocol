# W3CP-OCPP 1.6J Compatibility Layer

## Why Not DataTransfer?

**We do not use `DataTransfer` for W3CP extensions.** The OCPP `DataTransfer` mechanism was a flawed design decision — a generic escape hatch that led to fragmentation, confusion, and undefined behaviors across implementations. Instead of promoting interoperable extension patterns, `DataTransfer` encouraged ad hoc, vendor-specific hacks hidden behind stringly-typed blobs.

> **DataTransfer was a mistake.** It created more problems than it solved by violating core protocol principles: typed messages, clear directionality, and explicit semantics. Worse, many backends treat `DataTransfer` as untrusted or reject it entirely — making it useless for critical extensions like authentication.

**W3CP defines real messages with real types** — no `vendorId`, no `messageId`, no JSON-inside-JSON horror. These extensions are first-class OCPP 1.6J messages with clear semantics and predictable behavior.

---

## Overview

This document defines the custom OCPP 1.6J messages used to integrate W3CP authentication into a standard OCPP 1.6J flow. These messages allow a backend to cryptographically challenge and authenticate a Charge Point (CP) using the same identity proof mechanism as the W3CP protocol.

## Message Flow

1. CP connects via WebSocket using standard OCPP 1.6J.
2. CP sends a standard `BootNotification.req`.
3. Backend determines if identity challenge is required.
4. If challenge is required, backend responds to the first `BootNotification` with status `Pending`.
5. Backend immediately sends `SignedChallenge.req` (W3CP extension, custom OCPP message).
6. CP responds with `SignedChallenge.conf`, embedding the native W3CP `IdentityProof` message.
7. Backend verifies signature and optional proof-of-work.
8. If valid, CP sends a second `BootNotification.req`, which now receives status `Accepted`.
9. Normal OCPP operations may continue (e.g., Authorize, StartTransaction).

---

## Message: `SignedChallenge.req`

**Direction:** Central System → Charge Point
**Action Name:** `SignedChallenge`

### Payload

```json
{
  "nonce": "abc123",
  "timestamp": "2025-07-09T14:00:00Z",
  "difficulty": 0
}
```

| Field        | Type   | Description                                   |
| ------------ | ------ | --------------------------------------------- |
| `nonce`      | string | Unique challenge nonce                        |
| `timestamp`  | string | ISO 8601 timestamp of challenge issuance      |
| `difficulty` | int    | Optional PoW difficulty (0 = no PoW required) |

---

## Message: `SignedChallenge.conf`

**Direction:** Charge Point → Central System
**Action Name:** `SignedChallenge`

### Payload

This message embeds a complete W3CP `W3CPMessage<IdentityProof>` without additional wrapping or OCPP-specific translation. The `type` field is removed in favor of the already well-defined structure in the W3CP protocol.

```json
{
  "payload": {
    "cpId": "CP-12345",
    "timestamp": "2025-07-09T14:00:03Z",
    "nonce": "abc123",
    "identityType": "web3",
    "powNonce": 42
  },
  "payloadSignature": "0xabcdef...",
  "payloadSha256Hash": "deadbeef..."
}
```

| Field               | Type   | Description                                  |
| ------------------- | ------ | -------------------------------------------- |
| `payload`           | object | The IdentityProof object (see W3CP spec)     |
| `payloadSignature`  | string | Hex-encoded digital signature of the payload |
| `payloadSha256Hash` | string | SHA-256 hash of the payload (hex-encoded)    |

---

### Notes

* This mechanism is transport-agnostic. It can be used on top of standard WebSocket (WSS), with or without mTLS.

* For deployments using mTLS, the same CP can additionally present an X.509 certificate during the TLS handshake. This is not required but supported.

* The identity proof inside `SignedChallenge.conf` can also be of type `x509Certificate` and include a digest or reference to the presented TLS certificate.

* This makes the W3CP identity challenge mechanism compatible with strict PKI setups while avoiding mTLS complexity for lighter deployments.

* This extension does not rely on `DataTransfer` and uses proper OCPP request/response framing.

* Only Charge Points that are not implicitly trusted should be required to respond to a `SignedChallenge`.

* The response embeds native W3CP `IdentityProof` messages exactly as defined in the W3CP protocol, allowing backend-side verification to reuse existing W3CP logic.

* Backends must track whether a CP is authenticated before accepting critical messages like `Authorize`, `StartTransaction`, etc.

* All timestamps are in ISO 8601 Zulu format (e.g., `2025-07-09T14:00:00Z`)

## Message: `Heartbeat`

**Direction:** Charge Point → Central System
**Action Name:** `Heartbeat`

### Behavior

The backend always responds to `Heartbeat.req` with a `Heartbeat.conf` message containing the current backend time. There are no conditions, state changes, or validations associated with this message.

### Reasoning

The `Heartbeat` message is fundamentally useless. It serves no meaningful purpose beyond what the underlying TCP/WebSocket layer already provides. Its presence adds protocol noise without delivering any actionable insight or reliability improvement.

This message serves purely as a connectivity check. It is ignored for authentication, logging, or session state evaluation. It also has no relation to the CP’s authorization state — even unauthenticated CPs will receive a valid heartbeat response.

### Example Response

```json
{
  "currentTime": "2025-07-09T14:00:05Z"
}
```

---


