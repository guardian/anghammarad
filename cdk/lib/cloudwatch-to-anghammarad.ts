import type { GuStackProps } from "@guardian/cdk/lib/constructs/core";
import {GuAnghammaradTopicParameter, GuStack} from "@guardian/cdk/lib/constructs/core";
import {GuLambdaFunction} from "@guardian/cdk/lib/constructs/lambda";
import {GuardianAwsAccounts} from "@guardian/private-infrastructure-config";
import type { App } from "aws-cdk-lib";
import {AnyPrincipal} from "aws-cdk-lib/aws-iam";
import {Runtime} from "aws-cdk-lib/aws-lambda";
import {SnsEventSource} from "aws-cdk-lib/aws-lambda-event-sources";
import {Topic} from "aws-cdk-lib/aws-sns";

export class CloudwatchToAnghammarad extends GuStack {
  constructor(scope: App, id: string, props: GuStackProps) {
    super(scope, id, props);
    const topic = new Topic(this, "CloudWatchToAnghammaradTopic", {
      topicName: "cloudwatch-to-anghammarad"
    });
    const accountNumbers = Object.values(GuardianAwsAccounts) as string[];
    accountNumbers.map((accountNumber) => {
      // https://docs.aws.amazon.com/sns/latest/dg/sns-access-policy-use-cases.html#sns-allow-cloudwatch-alarm-to-publish-to-topic-in-another-account
      const principal = new AnyPrincipal()
          .withConditions({
            'ArnLike': {
              'aws:SourceArn': `arn:aws:cloudwatch:${this.region}:${accountNumber}:alarm:*`
            }
          });
      topic.grantPublish(principal);
    });


    const anghammaradTopicArn = GuAnghammaradTopicParameter.getInstance(this).valueAsString;
    const lambda = new GuLambdaFunction(this, "CloudWatchToAnghammaradLambda", {
      app: "cloudwatch-to-anghammarad",
      environment: {
        ANGHAMMARAD_SNS_TOPIC_ARN: anghammaradTopicArn,
      },
      fileName: "cloudwatch.jar",
      handler: "com.gu.cloudwatch.Lambda::handleRequest",
      runtime: Runtime.JAVA_11
    });
    lambda.addEventSource(new SnsEventSource(topic));

    const anghammaradTopic = Topic.fromTopicArn(
        this,
        'AnghammaradTopic',
        anghammaradTopicArn,
    );

    anghammaradTopic.grantPublish(lambda);

  }
}
