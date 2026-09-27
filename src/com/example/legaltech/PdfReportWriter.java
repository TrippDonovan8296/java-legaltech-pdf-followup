package com.example.legaltech;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PdfReportWriter {
  public Path write(MatterIntake matter, SignedDocument document, Path directory) throws IOException {
    Files.createDirectories(directory);
    String body = "Matter: " + matter.title() + "\\nDeadline: " + matter.deadline()
        + "\\nSigned document: " + document.filename();
    String pdf = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
        + "2 0 obj<</Type/Pages/Count 0/Kids[]>>endobj\n"
        + "% " + body.replace("%", "") + "\n%%EOF\n";
    Path output = directory.resolve(matter.id() + ".pdf");
    Files.writeString(output, pdf, StandardCharsets.US_ASCII);
    return output;
  }
}
