import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { CloudwatchToAnghammarad } from "./cloudwatch-to-anghammarad";

describe("The CloudwatchToAnghammarad stack", () => {
  it("matches the snapshot", () => {
    const app = new App();
    const stack = new CloudwatchToAnghammarad(app, "CloudwatchToAnghammarad", { stack: "deploy", stage: "TEST" });
    const template = Template.fromStack(stack);
    expect(template.toJSON()).toMatchSnapshot();
  });
});
