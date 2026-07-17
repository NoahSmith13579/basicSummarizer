package com.example.summarizer.lambda;


import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.example.summarizer.SummarizerApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LambdaStreamHandler implements RequestStreamHandler {
  private static SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler;

  static {
    try {
      handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(SummarizerApplication.class);
    } catch (ContainerInitializationException e) {
      throw new RuntimeException("Could not initialize Spring Boot application", e);
    }
  }

  @Override
  public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
    handler.proxyStream(input, output, context);
  }
}