# @guardian/anghammarad

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
