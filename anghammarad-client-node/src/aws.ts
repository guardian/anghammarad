import * as SNS from "aws-sdk/clients/sns";
import {
  CredentialProviderChain,
  EC2MetadataCredentials,
  EnvironmentCredentials,
  SharedIniFileCredentials,
} from "aws-sdk";

/**
 * Use this to make an SNS client, or provide your own.
 */
export function snsClient(credentialProvider: CredentialProviderChain): SNS {
  return new SNS({ region: "eu-west-1", credentialProvider });
}

export function credentialsProvider(): CredentialProviderChain {
  return new CredentialProviderChain([
    // EC2
    () => new EC2MetadataCredentials(),
    // Lambda
    () => new EnvironmentCredentials(""),
    // local
    () => new SharedIniFileCredentials({ profile: "deployTools" }),
  ]);
}
