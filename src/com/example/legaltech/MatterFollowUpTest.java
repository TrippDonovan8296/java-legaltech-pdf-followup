package com.example.legaltech;

import java.nio.file.Path;

public final class MatterFollowUpTest {
  public static void main(String[] args) throws Exception {
    MatterFollowUpService service = new MatterFollowUpService(new PdfReportWriter(),
        new InfraiEmailClient("test-key") {
          @Override public String send(String to, String subject, String text) { return "test-message"; }
        });
    MatterIntake matter = new MatterIntake("test", "student@example.com", "Course matter", "2026-11-02");
    String accepted = service.followUp(matter, new SignedDocument("signed.pdf", true), Path.of("build/test"));
    String rejected = service.followUp(matter, new SignedDocument("draft.pdf", false), Path.of("build/test"));
    if (!"test-message".equals(accepted) || !"REJECTED_UNSIGNED".equals(rejected))
      throw new AssertionError("signed-document decision changed");
    System.out.println("PASS signed document decision");
  }
}
