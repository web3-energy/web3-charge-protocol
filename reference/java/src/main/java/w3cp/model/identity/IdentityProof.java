package w3cp.model.identity;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import w3cp.model.identity.web3.Web3Identity;

/**
 * Request sent by a Charge Point to prove its identity.
 *
 * This message contains the identity information and a signed response
 * to the backend-issued challenge. If the backend requested proof-of-work
 * (difficulty > 0 in IdentityChallenge), the Charge Point must vary the payload
 * using `powNonce` until the SHA-256 hash of the serialized payload meets
 * the required difficulty (e.g., ends with N zero bits).
 *
 * ⚠️ This version is mutable and optimized for efficient PoW computation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityProof {

  private String cpId;
  private Instant timestamp;
  private String nonce;
  private IdentityType identityType;
  private Web3Identity web3Identity; // needed when IdentityType == web3
  private long powNonce;
}