# @guardian/anghammarad

## 3.0.0

### Major Changes

- c115d49: Remove singleton implementation and require an `SNSClient` on instantiation.

  Previously we were providing credentials via `fromNodeProviderChain`.
  This provider chain loads profile credentials before environment credentials when they're both set (e.g. within the AWS Lambda runtime),
  resulting in the following log:

  ```log
  @aws-sdk/credential-provider-node - defaultProvider::fromEnv WARNING:
      Multiple credential sources detected:
      Both AWS_PROFILE and the pair AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY static credentials are set.
      This SDK will proceed with the AWS_PROFILE value.

      However, a future version may change this behavior to prefer the ENV static credentials.
      Please ensure that your environment only sets either the AWS_PROFILE or the
      AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY pair.
  ```

  Within the AWS Lambda runtime, the `deployTools` profile doesn't actually evaluate to valid credentials, therefore the AWS calls fail.
  For this reason, an `SNSClient` is now required on instantiation.
  This user provided client should have the credential provider relevant to the user's runtime, thus making `@guardian/anghammarad` runtime agnostic.

  Also in this change is a reversion of the singleton implementation.
  The singleton was aimed at simplify the DX for users by automatically configuring the SNS topic via an SSM Parameter.
  However, in reality this wasn't an issue as users already have the SNS topic in their CloudFormation template in order to provide `sns:Publish` access.
  Now, when instantiating the Anghammarad client, an `SNSClient` and `topicArn` are required.

## 2.0.0

### Major Changes

- 7c76f58: Releasing @guardian/anghammarad v2.0.0

  This change updates to AWS SDK v3 and is published as ESM-only.
  Additionally, the `Anghammarad` class is now implemented as a singleton, with the SNS topic obtained from SSM Parameter Store; you no longer need to provide this.

  To send a message:

  ```ts
  import {
    Anghammarad,
    type AnghammaradNotification,
  } from "@guardian/anghammarad";
  const anghammarad = await Anghammarad.getInstance();
  const notification: AnghammaradNotification = {};
  await anghammarad.notify(notification);
  ```

### Patch Changes

- caf2058: no-op release to test migration to NPM trusted publishing

## 1.8.3

### Patch Changes

- b19c952: Update `ws` dependency to 7.5.10

## 1.8.2

### Patch Changes

- 492a2b2: Test noop
