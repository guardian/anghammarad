import { SNS } from "aws-sdk";

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
  client?: SNS;
  threadKey?: string;
}
