package w3cp.model.identity;

public enum IdentityType {
  /**
   * Raw public key identity.
   */
  publicKey,

  /**
   * X.509 certificate-based identity (e.g., TLS client cert).
   */
  x509Certificate,

  /**
   * KILT-based identity (Polkadot + KILT).
   */
  web3
}
