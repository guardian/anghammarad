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

For example

```js
import { Anghammarad, RequestedChannel } from '@guardian/anghammarad';

const client = new Anghammarad();

client.notify({
  subject: 'Hello',
  message: "Hi there, something has happened which we'd like to tell you about",
  actions: [{ url: 'https://example.com' }],
  target: { Stack: 'my-stack', Stage: 'CODE', App: 'my-app' },
  channel: RequestedChannel.Email,
  sourceSystem: 'my-monitoring-tool',
  topicArn: 'arn:aws:123',
});
```

Or providing the optional SNS client ([more details](#sns-client))

```js
import { Anghammarad, RequestedChannel } from '@guardian/anghammarad';
import { credentialsProvider, snsClient } from './aws';

const client = new Anghammarad();

client.notify({
  subject: 'Hello',
  message: "Hi there, something has happened which we'd like to tell you about",
  actions: [{ url: 'https://example.com' }],
  target: { Stack: 'my-stack', Stage: 'CODE', App: 'my-app' },
  channel: RequestedChannel.Email,
  sourceSystem: 'my-monitoring-tool',
  topicArn: 'arn:aws:123',
  client: snsClient(credentialsProvider()),
});
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
| client       | An optional SNS client to send the message with.                               | N        | https://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/SNS.html |

### Channels

Anghammarad can currently notify via either email or Google chat. The channel param can be used to pass the required notification channel. The prefix `prefer ` can be added to specify that where possible a particular channel should be used but if not, use another available channel. The `RequestedChannel` enum is provided with a list of available values.

### SNS Client

By default, an SNS client is created when you create an Anghammarad client. You can also optionally pass in your own SNS client either when creating the Anghammarad client or when sending a message, as shown in the examples below. You may want to do this if you need to provide any custom options to the SNS client either for all of the messages from you application or for specific messages.

```js
// Custom client on notify
import {
  Anghammarad,
  RequestedChannel,
} from "@guardian/anghammarad";
import { credentialsProvider, snsClient } from "./aws";

const client = new Anghammarad();

client.notify({
    subject: "Hello",
    message: "Hi there, something has happened which we'd like to tell you about",
    actions: [{url: "https://example.com"}],
    target: {Stack: "my-stack", Stage: "CODE", App: "my-app"},
    channel: RequestedChannel.Email,
    sourceSystem: "my-monitoring-tool",
    topicArn: "arn:aws:123",
    client: snsClient(credentialsProvider()
})
```

```js
// Custom client for Anghammarad client
import { Anghammarad, RequestedChannel } from '@guardian/anghammarad';
import { credentialsProvider, snsClient } from './aws';

const client = new Anghammarad(snsClient(credentialsProvider()));

client.notify({
  subject: 'Hello',
  message: "Hi there, something has happened which we'd like to tell you about",
  actions: [{ url: 'https://example.com' }],
  target: { Stack: 'my-stack', Stage: 'CODE', App: 'my-app' },
  channel: RequestedChannel.Email,
  sourceSystem: 'my-monitoring-tool',
  topicArn: 'arn:aws:123',
});
```

## Releasing the client

The client is published to npm as `@guardian/anghammarad`. You must have an `npm` account with 2fa enabled and be part of the `guardian` organisation. You can then run `npm version <patch|minor|major>` followed by `npm publish` to publish the library, enterring your OTP when prompted (issues have been encountered running `yarn publish` in the past so npm is recommended).
