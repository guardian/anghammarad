import {PublishCommand, SNSClient} from "@aws-sdk/client-sns";
import {fromNodeProviderChain} from "@aws-sdk/credential-providers";
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

export interface AnghammaradNotification {
 message: string;
 actions: Action[];
 target: Target;
 channel: RequestedChannel;
 sender: string;
 threadKey?: string;
}

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
     credentials: fromNodeProviderChain({profile: 'deployTools'})
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

  async notify(subject: string, body: AnghammaradNotification) {
   const command = new PublishCommand({
    TopicArn: this.topicArn,
    Subject: subject,
    Message: JSON.stringify(body),
   });

    const request = await this.snsClient.send(command);
    return request.MessageId;
  }
}
