import { NotifyParams } from "./interfaces";
import { SNS } from "aws-sdk";
import { credentialsProvider, snsClient } from "./aws";

export class Anghammarad {
  client: SNS;

  constructor(client = snsClient(credentialsProvider())) {
    this.client = client;
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
    const sns = this.client;
    const request = await sns
      .publish({
        TopicArn: params.topicArn,
        Subject: params.subject,
        Message: this.messageJson(params),
      })
      .promise();

    return request.MessageId;
  }
}

export * from "./interfaces";
