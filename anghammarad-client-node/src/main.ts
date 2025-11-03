import {PublishCommand, SNSClient} from "@aws-sdk/client-sns";
import {fromNodeProviderChain} from "@aws-sdk/credential-providers";

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

export interface NotifyParams {
 subject: string;
 message: string;
 actions: Action[];
 target: Target;
 channel: RequestedChannel;
 sourceSystem: string;
 topicArn: string;
 threadKey?: string;
}


export class Anghammarad {
  private readonly snsClient: SNSClient;

  constructor() {
   this.snsClient = new SNSClient({region: "eu-west-1", credentials: fromNodeProviderChain({profile: 'deployTools'})});
  }

  messageJson(params: NotifyParams): string {
    const {
      message,
      sourceSystem,
      channel,
      target,
      actions,
      threadKey
    } = params

    return JSON.stringify({
      message,
      sender: sourceSystem,
      channel,
      target,
      actions,
      ...(threadKey && { threadKey }), // only add "threadKey" when it is defined
    });
  }

  async notify(params: NotifyParams) {
   const command = new PublishCommand({
    TopicArn: params.topicArn,
    Subject: params.subject,
    Message: this.messageJson(params),
   });

    const request = await this.snsClient.send(command);
    return request.MessageId;
  }
}
