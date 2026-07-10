package com.example.summarizer.lambda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LocalLambdaTest {
  public static void main(String[] args) throws Exception {
    LambdaStreamHandler handler = new LambdaStreamHandler();

    String event = """
            {
              "version": "2.0",
              "routeKey": "GET /api/v1/summarize/health",
              "rawPath": "/api/v1/summarize/health",
              "rawQueryString": "",
              "headers": {
                "accept": "*/*"
              },
              "requestContext": {
                "http": {
                  "method": "GET",
                  "path": "/api/v1/summarize/health",
                  "protocol": "HTTP/1.1",
                  "sourceIp": "127.0.0.1",
                  "userAgent": "local-test"
                }
              },
              "isBase64Encoded": false
            }
            """;


    InputStream input = new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    handler.handleRequest(input, output, null);

    System.out.println(output.toString());
  }
}
