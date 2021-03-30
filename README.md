Anghammarad
===========

> …every undelivered message is a piece of space-time that lacks
> another end, a little bundle of effort and emotion floating freely.
> 
> ~ Terry Pratchett

Notification service for the Guardian's dev teams.

## Usage

### Scala Client

To find the latest version of Anghammard's client, see [here](https://github.com/guardian/anghammarad/releases).

[Changelist](CHANGES.md)

To use Anghammarad from your project, include its client library in
your sbt dependencies.

```
"com.gu" %% "anghammarad-client" % <latest-version>
```

The Anghammarad client contains a function that will send a notification.

```scala
import com.gu.anghammarad.Anghammarad
import com.gu.anghammarad.models._

Anghammarad.notify( ... )
```

Details about the function's arguments are available in the javadoc.

### Node Client

Follow the instructions in the [node client README](./anghammarad-client-node/README.md).

## Releasing the Scala client

You will need:

* Sonatype account that has been whitelisted for the Guardian's group
* A published PGP key
* Global SBT configuration to include your sonatype ceredentials

With the above in place, you can release a new version using sbt's
`release` command from the root project.

```bash
sbt release
```

The sbt-release plugin will suggest the next version number, but consider
what the next version number should be by considering the change against
[the semantic versioning guidelines](https://semver.org/).

## Releasing the Node client

Follow the instructions in the [node client README](./anghammarad-client-node/README.md)
