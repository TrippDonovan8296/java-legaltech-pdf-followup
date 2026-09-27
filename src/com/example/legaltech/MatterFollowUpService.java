package com.example.legaltech;

import java.io.IOException;
import java.nio.file.Path;

public final class MatterFollowUpService {
  private final PdfReportWriter reports;
  private final InfraiEmailClient email;

  public MatterFollowUpService(PdfReportWriter reports, InfraiEmailClient email) {
    this.reports = reports; this.email = email;
  }

  public String followUp(MatterIntake matter, SignedDocument document, Path output)
      throws IOException, InterruptedException {
    if (!document.signed()) return "REJECTED_UNSIGNED";
    reports.write(matter, document, output);
    return email.send(matter.recipient(), "Signed matter report: " + matter.title(),
        "Your signed document report for " + matter.title() + " is ready. Deadline: " + matter.deadline());
  }
}
