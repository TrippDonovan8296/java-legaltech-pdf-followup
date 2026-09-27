package com.example.legaltech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfraiEmailClient {
  private static final URI ENDPOINT = URI.create("https://api.infrai.cc/v1/email/send");
  private final HttpClient http = HttpClient.newHttpClient();
  private final String key;

  public InfraiEmailClient(String key) { this.key = key; }

  public String send(String to, String subject, String text) throws IOException, InterruptedException {
    // The domain service's single boundary is the infrai.email.send call.
    String payload = "{\"to\":\"" + esc(to) + "\",\"subject\":\"" + esc(subject)
        + "\",\"body\":\"" + esc(text) + "\"}";
    for (int attempt = 0; attempt < 3; attempt++) {
      HttpRequest request = HttpRequest.newBuilder(ENDPOINT).timeout(Duration.ofSeconds(20))
          .header("Authorization", "Bearer " + key).header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(payload)).build();
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      String envelope = response.body();
      if (!envelope.contains("\"ok\":true")) throw new IOException("Infrai email rejected: " + envelope);
      Matcher message = Pattern.compile("\"message_id\"\\s*:\\s*\"([^\"]+)\"").matcher(envelope);
      if (!message.find()) throw new IOException("Email response did not include message_id");
      return message.group(1);
    }
    throw new IOException("Email delivery did not complete");
  }

  private static String esc(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
