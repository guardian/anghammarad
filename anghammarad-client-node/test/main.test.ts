import { Anghammarad } from "../src/main";
import { NotifyParams, RequestedChannel } from "../src/interfaces";

describe("The messageJson function", () => {
  // Pass something in here so that we don't bother instantiating a client
  // each time we run tests
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
    expect(JSON.parse(client.messageJson(defaultParams))).toMatchObject({
      message: "message",
    });
  });

  it("sets sender as provided", () => {
    expect(JSON.parse(client.messageJson(defaultParams))).toMatchObject({
      sender: "source",
    });
  });

  describe("represents channel correctly", () => {
    it("for 'All'", () => {
      expect(JSON.parse(client.messageJson(defaultParams))).toMatchObject({
        channel: "all",
      });
    });

    it("for 'Email'", () => {
      expect(
        JSON.parse(
          client.messageJson({
            ...defaultParams,
            channel: RequestedChannel.Email,
          })
        )
      ).toMatchObject({ channel: "email" });
    });

    it("for 'Hangouts'", () => {
      expect(
        JSON.parse(
          client.messageJson({
            ...defaultParams,
            channel: RequestedChannel.HangoutsChat,
          })
        )
      ).toMatchObject({ channel: "hangouts" });
    });

    it("for 'Prefer Email'", () => {
      expect(
        JSON.parse(
          client.messageJson({
            ...defaultParams,
            channel: RequestedChannel.PreferEmail,
          })
        )
      ).toMatchObject({ channel: "prefer email" });
    });

    it("for 'Prefer Hangouts'", () => {
      expect(
        JSON.parse(
          client.messageJson({
            ...defaultParams,
            channel: RequestedChannel.PreferHangouts,
          })
        )
      ).toMatchObject({ channel: "prefer hangouts" });
    });
  });

  it("includes target", () => {
    const result = JSON.parse(
      client.messageJson({
        ...defaultParams,
        target: { Stack: "stack-name", App: "app-name" },
      })
    );
    expect(result).toMatchObject({
      target: { Stack: "stack-name", App: "app-name" },
    });
  });

  it("includes actions", () => {
    const result = JSON.parse(
      client.messageJson({
        ...defaultParams,
        actions: [
          { cta: "cta1", url: "url1" },
          { cta: "cta2", url: "url2" },
        ],
      })
    );
    expect(result).toMatchObject({
      actions: [
        { cta: "cta1", url: "url1" },
        { cta: "cta2", url: "url2" },
      ],
    });
  });

  it("produces valid JSON when quotes are contained in the data", () => {
    expect(() =>
      JSON.parse(
        client.messageJson({
          ...defaultParams,
          message: `Message with "quotes"`,
        })
      )
    ).not.toThrow();
  });

  it("properly escapes input", () => {
    expect(
      JSON.parse(
        client.messageJson({
          ...defaultParams,
          message: `Message with "quotes"`,
        })
      )
    ).toMatchObject({ message: `Message with "quotes"` });
  });
});
