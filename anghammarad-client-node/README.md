# Anghammarad Node Client

[Anghammarad](https://github.com/guardian/anghammarad) is a notification service for the Guardian's dev teams.

## Usage

To use Anghammarad from your project, include its client library in
your package.json.

`yarn add @guardian/anghammarad` or `npm install --save @guardian/anghammarad`

The Anghammarad client contains a function that will send a notification.

```js
import { Anghammarad } from '@guardian/anghammarad';

const client = new Anghammarad();
client.notify({ ...parameters });
```

### Parameters

| key          | description                                                                    | required | example                                                          |
| ------------ | ------------------------------------------------------------------------------ | -------- | ---------------------------------------------------------------- |
| subject      | The subject line of the message                                                | Y        | "An example alert"                                               |
| message      | The body of the message                                                        | Y        | "This is an example alert. Please ignore"                        |
| actions      | An array of objects containing a cta and/or a url                              | Y        | [{"url": "https://example.com}]                                  |
| target       | A target object containing one or more of Stage, Stack, App and Account number | Y        | {Stage: "CODE"}                                                  |
| channel      | One of the accepted channel types                                              | Y        | "prefer hangouts"                                                |
| sourceSystem | The name of the process sending the alert                                      | Y        | "my app"                                                         |
| topicArn     | The ARN of the topic to which the message should be send                       | Y        | "arn:aws:..."                                                    |
| client       | An optional SNS client to send the message with                                | N        | https://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/SNS.html |

### Channels

Anghammarad can currently notify via either email or Google chat. The channel param can be used to pass the required notification channel. The prefix `prefer ` can be added to specify that where possible a particular channel should be used but if not, use another available channel. The `RequestedChannel` enum is provided with a list of available values.

## Releasing the client

The client is published to npm as `@guardian/anghammarad`. You must have an `npm` account with 2fa enabled and be part of the `guardian` organisation. You can then run `yarn publish` to publish the library, enterring your OTP when prompted.
