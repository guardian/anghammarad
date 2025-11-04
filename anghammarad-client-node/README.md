# Anghammarad Node Client

[Anghammarad](https://github.com/guardian/anghammarad) is a notification service for the Guardian's dev teams.

## Usage

To use Anghammarad from your project, include its client library in
your package.json.

`yarn add @guardian/anghammarad` or `npm install --save @guardian/anghammarad`

The Anghammarad client contains a function that will send a notification.

```js
import { Anghammarad } from '@guardian/anghammarad';

const client = await Anghammarad.getInstance();
await client.notify("A message for you", { ...parameters });
```

For example

```js
import { Anghammarad, RequestedChannel } from '@guardian/anghammarad';

const client = await Anghammarad.getInstance();

await client.notify("A message for you", {
  message: "Hi there, something has happened which we'd like to tell you about",
  actions: [{ url: 'https://example.com' }],
  target: { Stack: 'my-stack', Stage: 'CODE', App: 'my-app' },
  channel: RequestedChannel.Email,
  sender: 'my-monitoring-tool',
});
```

### Parameters

| key     | description                                                                    | required | example                                                          |
|---------| ------------------------------------------------------------------------------ | -------- | ---------------------------------------------------------------- |
| message | The body of the message                                                        | Y        | "This is an example alert. Please ignore"                        |
| actions | An array of objects containing a cta and/or a url                              | Y        | [{"url": "https://example.com}]                                  |
| target  | A target object containing one or more of Stage, Stack, App and Account number | Y        | {Stage: "CODE"}                                                  |
| channel | One of the accepted channel types                                              | Y        | "prefer hangouts"                                                |
| sender  | The name of the process sending the alert                                      | Y        | "my app"                                                         |

### Channels

Anghammarad can currently notify via either email or Google chat. The channel param can be used to pass the required notification channel. The prefix `prefer ` can be added to specify that where possible a particular channel should be used but if not, use another available channel. The `RequestedChannel` enum is provided with a list of available values.

## Releasing the client
On merge to `main`, the client is published to npm as `@guardian/anghammarad` using [`changesets`](https://github.com/changesets/changesets).

### Beta release
To create a beta release, add the `[beta] @guardian/anghammarad` label to your PR.
A comment will be added to the PR with the version number of your beta release.