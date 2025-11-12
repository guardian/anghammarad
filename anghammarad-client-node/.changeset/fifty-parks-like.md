---
"@guardian/anghammarad": minor
---

Require a credential provider to be provided.

It appears that the `fromNodeProviderChain` uses profile credentials before environment credentials.
With a `profile` being passed to `fromNodeProviderChain`, using the library within an AWS Lambda function resulted in an authentication failure and the following log:

```log
@aws-sdk/credential-provider-node - defaultProvider::fromEnv WARNING:
    Multiple credential sources detected:
    Both AWS_PROFILE and the pair AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY static credentials are set.
    This SDK will proceed with the AWS_PROFILE value.

    However, a future version may change this behavior to prefer the ENV static credentials.
    Please ensure that your environment only sets either the AWS_PROFILE or the
    AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY pair.
```

This update asks clients to provide their own credential provider allowing the library to become agnostic of the runtime environment.