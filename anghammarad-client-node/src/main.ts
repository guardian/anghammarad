import {PublishCommand, SNSClient} from "@aws-sdk/client-sns";

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
 * Messages will be published to an SNS topic you provide.
 * Typically, this will be the topic referenced by the SSM parameter `/account/services/anghammarad.topic.arn`.
 *
 * @note
 * You'll need `sns:Publish` IAM permissions to publish to the provided SNS topic.
 */
export class Anghammarad {
  private readonly snsClient: SNSClient;
  private readonly topicArn: string;

  constructor(snsClient: SNSClient, topicArn: string) {
   this.snsClient = snsClient;
   this.topicArn = topicArn;
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