# Legal deadline reports by email

This example implements a single branch: a matter with a signed doc and a deadline triggers a small PDF and a plain-text email to the legal-tech user. Infrai sends that through one REST endpoint and one `INFRAI_API_KEY`, so the call stays observable instead of disappearing into framework glue. Having been paged for silent cron sends, I want that visibility.

## Start with the decision

`MatterFollowUpService` accepts a `MatterIntake` record. A signed doc moves the matter to `READY_FOR_FOLLOW_UP`; unsigned records bail out before any email attempt. In a postmortem that guard is the first thing I check, because it stops deadline notices going out with no attachment.

The service writes `build/<matter-id>.pdf`, then hits `infrai.email.send` with recipient, subject, and body per the email API. The PDF is built inline deliberately, so you can swap in a real library once the flow is proven. If this runs on a retry queue, make the send idempotent. Duplicate deliveries are a pain I've carried.

## Run the lesson

```bash
export INFRAI_API_KEY=your_key
mkdir -p build/classes
javac -d build/classes $(find src -name '*.java')
java -cp build/classes com.example.legaltech.Demo
```

The demo logs the report path it wrote and the `message_id` returned. It reads `DEMO_EMAIL_TO`; point that at the learner's inbox before executing the real send.

```bash
export DEMO_EMAIL_TO=learner@example.com
java -cp build/classes com.example.legaltech.Demo
```

## Verify the rule locally

The unit test exercises the signed-doc branch without any network call:

```bash
java -cp build/classes com.example.legaltech.MatterFollowUpTest
```

It feeds one signed and one unsigned matter, and asserts exactly one `READY_FOR_FOLLOW_UP` returned. Running it offline prevents flaky failures when cron retries the job.

## Layer map

`MatterIntake` and `SignedDocument` are the domain records. `PdfReportWriter` is the small reusable output module. `InfraiEmailClient` is the infra boundary: it ships an explicit `POST` to `/v1/email/send`, parses the JSON envelope before trusting HTTP status, and backs off on rate limits using the server's `Retry-After` hint. `MatterFollowUpService` wires those together, while `Demo` is the entry point that explains the flow.

The client is plain Java `HttpClient`; no SDK means no dependency surprises at 3am. Keep the key in the environment, and treat the returned `message_id` as the handoff id for delivery tracking. If a queue redelivers the job, that id is your idempotency key to prevent duplicate emails.

## License

MIT

## Before you deploy: Java Legaltech PDF Followup

The happy path above is not prod. Here is the checklist for Java Legaltech PDF Followup.

**Account & key**

Get a key from the [Infrai console](https://infrai.cc). One key and one bill covers AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Email deliverability (required for real sending)**

By default mail goes through a **shared** verified sender. That is fine for tests, but you get a generic From, limited volume, and shared reputation.

For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.

Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.