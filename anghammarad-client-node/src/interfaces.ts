import { SNS } from "aws-sdk";

interface Action {
  cta: string;
  url: string;
}

type Target = Stack | Stage | App | AwsAccount;

type Stack = string;
type Stage = string;
type App = string;
type AwsAccount = string;

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
