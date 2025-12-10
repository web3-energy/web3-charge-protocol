package w3cp.model.identity.web3;

/**
 * Web3 DID identity for W3CP.
 *
 */
public record Web3Identity(
    Web3IdentityMethod method,  // kilt | ewc
    String did,                 // canonical DID string
    String kid                  // optional key fragment (e.g., "#key-1"); may be null
) {
  public enum Web3IdentityMethod {
    ewc,   // Energy Web Chain (ethr-did)
    kilt,   // KILT DID (light/full)
    polkadot
  }
}