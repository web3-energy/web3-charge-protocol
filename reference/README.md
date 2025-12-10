# W3CP Reference DTOs
Reference Data Transfer Objects (DTOs) for the Web3 Charge Protocol – Clean, typed, and ready-to-use message structures that make W3CP integration straightforward.

# Why Reference DTOs
### Because implementation should be simple.

Building with W3CP shouldn't require guesswork or reverse-engineering from specifications.

These reference DTOs provide **concrete, typed implementations** of every W3CP message, identity structure, and capability definition. They're designed to be copied, extended, or used as-is — whatever gets you building faster.

---

## Built for Developers

These DTOs are **production-ready** and **specification-compliant**.  
They handle the complexity so you don't have to:
- Complete message serialization/deserialization
- Identity proof structures for Web3, X.509, and public key authentication
- Smart capability definitions
- Charging session and status management
- Diagnostic and monitoring structures

No more implementing the same message structures over and over.

---

## Identity First, Always

Every DTO reflects W3CP's **identity-first** philosophy.  
Authentication and authorization structures are first-class citizens:
- `IdentityProof` for cryptographic authentication
- `IdentityChallenge` for secure handshakes  
- `Web3Identity`, `X509Identity`, and `PublicKeyIdentity` for different trust models
- `W3CPPrivateKey` and `W3CPPublicKey` for key management

Trust is built into the data structures themselves.

---

## What's Inside

| Package | Purpose |
|---------|---------|
| **`identity/`** | Complete identity and authentication structures |
| **`capabilities/`** | Smart capability definitions and negotiation |
| **`status/`** | Charge point, connector, and session status |
| **`diagnostics/`** | System monitoring and diagnostic messages |
| **`iso15118/`** | ISO 15118 integration and state management |
| **Core** | Base message types and connection management |

---

## Simple to Use, Powerful by Design

Start with the basics:
```java
W3CPMessage<IdentityProof> authMessage = new W3CPMessage<>();
authMessage.setType(W3CPMessageType.IDENTITY_PROOF);
authMessage.setPayload(identityProof);
```

Extend as needed:
```java
SmartCapabilities capabilities = new SmartCapabilities();
capabilities.setSupportsWeb3Identity(true);
capabilities.setSupportsIso15118(true);
```

Scale to full implementations:
```java
ChargeSession session = new ChargeSession();
session.setEvInfo(evInfo);
session.setEnergyStatus(energyStatus);
```

---

## Language Support

Currently available:
- **Java** - Complete reference implementation

Coming soon:
- TypeScript/JavaScript
- Python  
- Go
- Rust

---

## The Problem with Protocol Implementation — and How Reference DTOs Fix It

| ⚠️ Manual Implementation | 🚀 Reference DTOs |
|--------------------------|-------------------|
| **Reinventing the wheel** — every project implements the same message structures differently. | **Standardized structures.** Copy, extend, or reference — your choice. |
| **Specification drift** — hand-coded messages diverge from the spec over time. | **Specification-compliant.** Generated and validated against the official W3CP spec. |
| **Type safety nightmares** — stringly-typed JSON leads to runtime errors. | **Strongly typed.** Compile-time safety and IDE support out of the box. |
| **Serialization complexity** — custom JSON handling for every message type. | **Serialization included.** Works with standard JSON libraries seamlessly. |
| **Testing difficulties** — hard to create valid test messages without deep spec knowledge. | **Test-ready.** Use reference DTOs to generate valid test cases instantly. |

---

## Ready to Build

These DTOs are **living references** — they evolve with the W3CP specification.  
Use them as:
- **Direct dependencies** in your projects
- **Code generation templates** for other languages  
- **Specification validation** against your custom implementations
- **Testing fixtures** for integration and unit tests

---

### Reference DTOs are for those who want to build, not debug message formats.
For those who value type safety.  
For those who ship fast.  
For those who focus on features, not protocol plumbing.

**Implementation. Simplified.**