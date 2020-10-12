import {Anghammarad} from "../src/main";
import {NotifyParams, RequestedChannel} from "../src/interfaces";

describe("The messageJson function", () => {
  // Pass something in here so that we don't bother instantiating a client
  // each time we run the tests
  const client = new Anghammarad({} as any);

  const defaultParams: NotifyParams = {
    subject: "subject",
    message: "message",
    actions: [{ cta: "cta", url: "url" }],
    target: { Stack: "stack" },
    channel: RequestedChannel.All,
    sourceSystem: "source",
    topicArn: "arn",
  };

  it("sets message as provided", () => {
    expect(client.messageJson(defaultParams)).toContain(`"message":"message"`);
  });

  it("sets sender as provided", () =>{
    expect(client.messageJson(defaultParams)).toContain(`"sender":"source"`)
  })

  describe("represents channel correctly", () => {
    it("for 'All'", () => {
      expect(client.messageJson(defaultParams)).toContain(`"channel":"all"`)
    })

    it("for 'Email'", () => {
      expect(client.messageJson({...defaultParams, channel: RequestedChannel.Email})).toContain(`"channel":"email"`)
    })

    it("for 'Hangouts'", () => {
      expect(client.messageJson({...defaultParams, channel: RequestedChannel.HangoutsChat})).toContain(`"channel":"hangouts"`)
    })

    it("for 'Prefer Email'", () => {
      expect(client.messageJson({...defaultParams, channel: RequestedChannel.PreferEmail})).toContain(`"channel":"prefer email"`)
    })

    it("for 'Prefer Hangouts'", () => {
      expect(client.messageJson({...defaultParams, channel: RequestedChannel.PreferHangouts})).toContain(`"channel":"prefer hangouts"`)
    })
  })

  it("includes target", () => {
    const result = client.messageJson({...defaultParams, target: {Stack: "stack-name", App: "app-name"}})
    expect(result).toContain(`"Stack":"stack-name"`)
    expect(result).toContain(`"App":"app-name"`)
  })

  it("includes actions", () => {
    const result = client.messageJson({...defaultParams, actions: [{cta: "cta1", url: "url1"}, {cta: "cta2", url: "url2"}]})
    expect(result).toContain(`"cta":"cta1"`)
    expect(result).toContain(`"url":"url1"`)
    expect(result).toContain(`"cta":"cta2"`)
    expect(result).toContain(`"url":"url2"`)
  })

  it("properly escapes input", () => {
    expect(client.messageJson({...defaultParams, message: `Message with "quotes"`})).toContain(`\"`)
  })
})
