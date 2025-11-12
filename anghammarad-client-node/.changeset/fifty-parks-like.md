---
"@guardian/anghammarad": major
---

Remove singleton implementation and require an `SNSClient` on instantiation.

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