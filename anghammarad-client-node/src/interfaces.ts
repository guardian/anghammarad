import { SNS } from "aws-sdk";

export interface Action {
  cta: string;
  url: string;
}

export interface Target {
  Stack?: string,
  Stage?: string,
  App?: string,
  AwsAccount?: string
}

// TODO: Implement this
interface RequestedChannel {}

export interface NotifyParams {
  subject: string;
  message: string;
  actions: Action[];
  target: Target[];
  channel: RequestedChannel;
  sourceSystem: string;
  topicArn: string;
  client?: SNS;
}
