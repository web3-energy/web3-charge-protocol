# W3CP Reference DTOs
Typed, consistent, specification-aligned data structures for implementing the Web3 Charge Protocol.

## Why These DTOs Exist
Protocol implementations break when every team hand-codes message formats differently.  
These DTOs provide **one canonical, strongly typed reference** — eliminating ambiguity, spec drift, and ad-hoc JSON.

Use them to build faster and stay aligned with the official W3CP spec.

---

## What’s Included
- **Identity structures** — Web3, X.509, and public-key identities
- **Authentication flows** — challenge, proof, and secure handshake
- **Capabilities** — charger features and negotiation
- **Status models** — system, port, and charging session state
- **Diagnostics** — monitoring, health, and system reports
- **ISO 15118 hooks** — integration-ready DTOs

Everything needed for CP firmware or backend integration.

---

## Developer-Friendly by Design
Strong typing, clear structures, and JSON-ready DTOs make the protocol simple to integrate and hard to misuse.

### Example

```java
W3CPMessage<IdentityProof> msg = new W3CPMessage<>();
msg.setType(W3CPMessageType.IDENTITY_PROOF);
msg.setPayload(identityProof);
```

---

## Package Overview

| Package        | Purpose                                   |
|----------------|-------------------------------------------|
| `identity/`    | Identity & authentication DTOs            |
| `capabilities/`| Feature negotiation & charger capabilities|
| `status/`      | Charger, port & session state models      |
| `diagnostics/` | System diagnostics & health               |
| `iso15118/`    | ISO 15118 integration points              |
| *Core*         | Base message and transport types          |

---

## Language Support
Currently:
- **Java** — complete reference implementation

Planned:
- TypeScript
- Python
- Go
- Rust

---

## Solving Real Protocol Problems

| Without DTOs                                | With Reference DTOs                        |
|---------------------------------------------|--------------------------------------------|
| Inconsistent message formats                | Canonical, spec-aligned structures         |
| Spec drift over time                        | Single source of truth                     |
| Runtime errors from handwritten JSON        | Strong typing, compile-time checks         |
| Hard to generate valid test messages        | DTOs provide ready-made valid fixtures     |
| Rewriting structures in every codebase      | Drop-in, extendable message definitions    |

---

## Build With Confidence
These DTOs evolve with the W3CP specification and serve as the foundation for:

- Direct integration
- Code generation for other languages
- Testing fixtures
- Specification validation

**Implementation simplified.**
