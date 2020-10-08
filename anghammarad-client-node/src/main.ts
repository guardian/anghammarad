import { NotifyParams } from "./interfaces";
import { SNS } from "aws-sdk";
import { credentialsProvider, snsClient } from "./aws";

export class Anghammarad {
  client: SNS;

  constructor(client = snsClient(credentialsProvider())) {
    this.client = client;
  }

  messageJson(params: NotifyParams): string {
    return JSON.stringify({
      message: params.message,
      sender: params.sourceSystem,
      channel: params.channel,
      target: params.target,
      actions: params.actions,
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
