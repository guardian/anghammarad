import {PublishCommand, SNSClient} from "@aws-sdk/client-sns";
import {
 createCredentialChain, fromContainerMetadata,
 fromEnv,
 fromIni,
 fromInstanceMetadata,
} from "@aws-sdk/credential-providers";
import {GetParameterCommand, SSMClient} from "@aws-sdk/client-ssm";

export interface Action {
 cta: string;
 url: string;
}

export interface Target {
 Stack?: string;
 Stage?: string;
 App?: string;
 AwsAccount?: string;
 GithubTeamSlug?: string;
}

export enum RequestedChannel {
 All = "all",
 PreferHangouts = "prefer hangouts",
 PreferEmail = "prefer email",
 Email = "email",
 HangoutsChat = "hangouts",
}

interface AnghammaradSnsPayload {
 message: string;
 actions: Action[];
 target: Target;
 channel: RequestedChannel;
 sender: string;
 threadKey?: string;
}

export interface AnghammaradNotification extends AnghammaradSnsPayload {
 subject: string;
}

/**
 * Send notifications to teams via Anghammarad.
 * Messages will be published to the SNS topic referenced by the SSM parameter `/account/services/anghammarad.topic.arn`.
 *
 * This class is a singleton, use `getInstance` to get the instance.
 *
 * @usage
 * ```ts
 * import { Anghammarad, type AnghammaradNotification } from "@guardian/anghammarad";
 * const anghammarad = await Anghammarad.getInstance();
 * await anghammarad.notify({ ... });
 * ```
 *
 * @note
 * The following IAM permissions are required:
 * - `ssm:GetParameter` to read the SSM parameter `/account/services/anghammarad.topic.arn` storing the SNS topic ARN
 * - `sns:Publish` to publish to SNS
 */
export class Anghammarad {
  private readonly snsClient: SNSClient;
  private readonly topicArn: string;
  private static instance: Anghammarad | undefined;

  private constructor(snsClient: SNSClient, topicArn: string) {
   this.snsClient = snsClient;
   this.topicArn = topicArn;
  }

  public static async getInstance() {
   if(!this.instance) {
    const awsConfiguration = {
     region: "eu-west-1",
     credentials: createCredentialChain(
      // EC2
      fromInstanceMetadata(),

      // ECS
      fromContainerMetadata(),

      // Lambda
      fromEnv(),

      // Local
      fromIni({profile: 'deployTools'})
     )
    };

    const snsClient = new SNSClient(awsConfiguration);
    const ssmClient = new SSMClient(awsConfiguration);

    const parameterName = "/account/services/anghammarad.topic.arn";
    const command = new GetParameterCommand({
     Name: parameterName,
    });

    const { Parameter } = await ssmClient.send(command);

    if(!Parameter || !Parameter.Value) {
     throw new Error(`Unable to read SSM parameter ${parameterName}`);
    }

    this.instance = new Anghammarad(snsClient, Parameter.Value);
   }

   return this.instance;
  }

  async notify(notification: AnghammaradNotification) {
   const { subject, ...snsPayload } = notification;

   const command = new PublishCommand({
    TopicArn: this.topicArn,
    Subject: subject,
    Message: JSON.stringify(snsPayload),
   });

    const request = await this.snsClient.send(command);
    return request.MessageId;
  }
}
