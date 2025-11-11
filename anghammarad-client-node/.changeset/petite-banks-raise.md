---
"@guardian/anghammarad": major
---

Releasing @guardian/anghammarad v2.0.0

This change updates to AWS SDK v3 and is published as ESM-only.
Additionally, the `Anghammarad` class is now implemented as a singleton, with the SNS topic obtained from SSM Parameter Store; you no longer need to provide this.

To send a message:

```ts
import { Anghammarad, type AnghammaradNotification } from '@guardian/anghammarad';
const anghammarad = await Anghammarad.getInstance();
const notification: AnghammaradNotification = { };
await anghammarad.notify(notification);
```