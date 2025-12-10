package w3cp.model;

/**
 * Known W3CP message types used in protocol communication.
 */
public enum W3CPMessageType {
  identityChallenge,
  identityProof,
  connectionStatus,
  messageError,
  chargepointStatus,
  authorizationRequest,
  authorizationResponse,
  describeVariables,
  smartCapabilities,
  identityDiscovery,
  identityReport
}
