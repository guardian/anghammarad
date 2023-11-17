import "source-map-support/register";
import { GuRoot } from "@guardian/cdk/lib/constructs/root";
import { CloudwatchToAnghammarad } from "../lib/cloudwatch-to-anghammarad";

const app = new GuRoot();
new CloudwatchToAnghammarad(app, "CloudwatchToAnghammarad-CODE", { stack: "deploy", stage: "CODE", env: { region: "eu-west-1" } });
new CloudwatchToAnghammarad(app, "CloudwatchToAnghammarad-PROD", { stack: "deploy", stage: "PROD", env: { region: "eu-west-1" } });
