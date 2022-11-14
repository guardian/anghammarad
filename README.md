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

### Mappings

You can find Anghammarad's latest mappings of AWS account to email address in Anghammarad's config, which are kept in S3. You will need Janus credentials for the deploy tools account to access this.

### Required AWS permissions 

To use Anghammarad your project will need AWS IAM permissions to publish to an SNS topic. This change in your project's cloudformation will be a AWS::IAM::Policy
and it will require the sns:Publish action. Here is an [example](https://github.com/guardian/security-hq/blob/f2486009cd115eb6b8af8bae42fd8421e03a4e6c/cloudformation/watched-account.template.yaml#L194).

### Testing your notification

A test [google group](https://groups.google.com/a/guardian.co.uk/g/anghammarad.test.alerts) has been created, so that you can test your notification. The stack for this is called "testing-alerts". You can also find this in the Anghammarad config in S3.

You can try out the `CODE` deployment before promoting to `PROD`, by obtaining "Deploy Tools" developer credentials from Janus and running:

```shell
sbt "project dev" "run fields --config-stage CODE --targets Stack=testing-alerts --subject my_test_message --message testing --source test_user --email"
```

This will send a message to the test group indicated above.

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
