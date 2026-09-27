package com.example.legaltech;

import java.nio.file.Path;

public final class Demo {
  public static void main(String[] args) throws Exception {
    String key = System.getenv("INFRAI_API_KEY");
    String to = System.getenv("DEMO_EMAIL_TO");
    if (key == null || key.isBlank() || to == null || to.isBlank())
      throw new IllegalStateException("INFRAI_API_KEY and DEMO_EMAIL_TO are required");
    MatterIntake matter = new MatterIntake("matter-101", to, "Northwind intake", "2026-10-01");
    String id = new MatterFollowUpService(new PdfReportWriter(), new InfraiEmailClient(key))
        .followUp(matter, new SignedDocument("agreement.pdf", true), Path.of("build"));
    System.out.println("Generated build/matter-101.pdf; message_id=" + id);
  }
}
