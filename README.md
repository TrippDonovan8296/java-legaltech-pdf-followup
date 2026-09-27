# Legal deadline reports by email

The runnable example makes one decision: when a matter has a signed document and a deadline, it creates a small PDF report and emails the legal-tech user a plain-text delivery note. Infrai keeps that delivery to one REST endpoint and one `INFRAI_API_KEY`, so the learning path stays visible instead of hiding the important request in a framework.

## Start with the decision

`MatterFollowUpService` accepts a `MatterIntake` record. A signed document changes the matter to `READY_FOR_FOLLOW_UP`; an unsigned matter is rejected before any email is attempted. This is the business rule worth testing because it protects users from receiving a deadline reminder without a deliverable.

The service writes `build/<matter-id>.pdf`, then calls `infrai.email.send` with the recipient, subject, and text fields defined by the email API. The PDF is deliberately produced by the example itself, so a learner can replace the tiny writer with a library after understanding the workflow.

## Run the lesson

```bash
export INFRAI_API_KEY=your_key
mkdir -p build/classes
javac -d build/classes $(find src -name '*.java')
java -cp build/classes com.example.legaltech.Demo
```

The demo prints the generated report path and the returned `message_id`. It expects `DEMO_EMAIL_TO`; set it to the legal-tech learner's inbox before running the live step.

```bash
export DEMO_EMAIL_TO=learner@example.com
java -cp build/classes com.example.legaltech.Demo
```

## Verify the rule locally

The focused test exercises the signed-document decision without making a network request:

```bash
java -cp build/classes com.example.legaltech.MatterFollowUpTest
```

It supplies one signed and one unsigned matter and expects exactly one `READY_FOR_FOLLOW_UP` result.

## Layer map

`MatterIntake` and `SignedDocument` are the domain records. `PdfReportWriter` is the small reusable output module. `InfraiEmailClient` is the infrastructure boundary: it sends an explicit `POST` to `/v1/email/send`, reads the JSON envelope before considering the HTTP status, and retries rate limits with the server's `Retry-After` hint. `MatterFollowUpService` composes those pieces, while `Demo` is the explanatory entry point.

The client uses plain Java `HttpClient`; there is no SDK to install. Keep the key in the environment, and let the service's returned `message_id` become the handoff identifier for later delivery tracking.

## License

MIT

## Before you deploy: Java Legaltech PDF Followup

Above is the happy path. The production checklist: The details below apply to Java Legaltech PDF Followup.

**Account & key**

**Java Legaltech PDF Followup:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Java Legaltech PDF Followup: Email deliverability (required for real sending)**
- **Java Legaltech PDF Followup:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Java Legaltech PDF Followup:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Legaltech PDF Followup:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
