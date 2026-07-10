FROM public.ecr.aws/lambda/java:17

COPY build/libs/summarizer-0.0.1-SNAPSHOT-all.jar /var/task/lib/app.jar

ENV JAVA_TOOL_OPTIONS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

CMD ["com.example.summarizer.lambda.LambdaStreamHandler::handleRequest"]
