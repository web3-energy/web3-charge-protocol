package w3cp.model.identity;


public record W3CPPublicKey(
    KeyType type,
    KeyEncoding encoding,
    String value
) {
  public enum KeyType {
    ed25519,     // KILT (JWS: EdDSA)
    secp256k1,   // EWC / ethr-did (JWS: ES256K)
    ecP256       // Enterprise X.509 (JWS: ES256)
  }

  public enum KeyEncoding {
    base64url
  }
}
