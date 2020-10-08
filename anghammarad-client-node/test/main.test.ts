import {Anghammarad} from "../src/main";
import {NotifyParams} from "../src/interfaces";

describe("The messageJson function", () => {
    const client = new Anghammarad()

    const defaultParams: NotifyParams = {
        subject: "subject",
        message: "message",
        actions: [{cta: "cta", url: "url"}],
        target: [{Stack: "stack"}],
        channel: {},
        sourceSystem: "source",
        topicArn: "arn"
    }

    it("sets message as provided", () => {
        expect(client.messageJson(defaultParams)).toContain(`"message":"message"`)
    })
})