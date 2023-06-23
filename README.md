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

You can try out your local changes by obtaining "Deploy Tools" developer credentials from Janus and running:

```shell
sbt "project dev" "run fields --config-stage CODE --targets Stack=testing-alerts --subject my_test_message --message testing --source test_user --email"
```

This will use `AnghammaradService.run` to send a message to the test group indicated above.

If you need to invoke the Lambda function (for example to test IAM permissions), in `CODE` or `PROD` you can also publish a message to one of Anghammarad's SNS topics by running:

```shell
# Set the ENV ("CODE" or "PROD")
ENV="CODE"
# Get the SNS topic for a particular environment from SSM: 
TOPIC_ARN=$(aws ssm get-parameter --name /$ENV/deploy/amigo/anghammarad.sns.topicArn --region eu-west-1 --profile deployTools | jq -r ".Parameter.Value")
# Send a notification to the topic using the --use-topic $TOPIC_ARN flag
sbt "project dev" "run fields --config-stage CODE --targets Stack=testing-alerts --subject my_test_message --message testing --source test_user --email --use-topic $TOPIC_ARN"
```

This will use `Anhammarad.notify` from the client project, allowing you to test it is working as expected as well as being able to test `CODE` & `PROD` are behaving.

### Node Client

Follow the instructions in the [node client README](./anghammarad-client-node/README.md).

## Releasing the Scala client

You will need:

* Sonatype account that is a member of the Guardian's group
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
