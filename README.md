# log4j2elk

`Log4j2Elk` can be used to index Log4j2 messages in [Elasticsearch](https://www.elastic.co/elasticsearch).

## Programmatic Configuration

[`ElkConfiguration`](src/main/java/com/dsingley/log4j2elk/ElkConfiguration.java) provides a builder interface for
configuring the operation of `Log4j2Elk` including two required fields:
- `baseUrl`
- `indexName`

```java
public static void main(String[] args) {
    ElkConfiguration configuration = ElkConfiguration.builder()
            .baseUrl("http://elasticsearch:9200")
            .indexName("example-index-name")
            .build();
    Log4j2Elk.configure(configuration);
}
```

## Environment Variable Configuration

Configuring `Log4j2Elk` consistently across multiple applications or services within your organization to forward
message to [log data streams](https://www.elastic.co/guide/en/elasticsearch/reference/current/logs-data-stream.html)
may be accomplished using an [`EnvironmentVariableElkConfigurationProvider`](src/main/java/com/dsingley/log4j2elk/EnvironmentVariableElkConfigurationProvider.java)
which externalizes most configuration options and will add three additional fields to indexed documents.

Configure a "service" or application name in your code and then set environment variables appropriate for your
deployment(s):

- `ELK_ELASTICSEARCH_BASE_URL=http://elasticsearch:9200` (required)
- `ELK_ENABLED=true`
- `ELK_ENVIRONMENT=production`

```java
public static void main(String[] args) {
    Log4j2Elk.configure(new EnvironmentVariableElkConfigurationProvider("example-service"));
}
```

The environment variables and single line of code shown above would results in log messages being added to a log data
stream `logs-exampleservice-production` with the following additional fields:

| index field | value                                              |
|:------------|:---------------------------------------------------|
| service     | `example-service`                                  |
| environment | `production`                                       |
| instance    | `24cd960f31694a11a982a09a3554e6c9` (a random UUID) |

## Tuning [`HttpAppender`](https://logging.apache.org/log4j/2.x/manual/appenders/network.html#HttpAppender) and [`AsyncAppender`](https://logging.apache.org/log4j/2.x/manual/appenders/delegating.html#AsyncAppender)

`Log4j2Elk` relies on these two Log4j components to forward messages to Elasticsearch and has selected several default
configuration values that are different from their Log4j default values. These values are configurable by both methods
shown above, are documented in [`ElkConfiguration`](src/main/java/com/dsingley/log4j2elk/ElkConfiguration.java), and
summarized below.

| log4j2elk parameter<sup>*</sup> | log4j2 default       | log4j2elk default (if different) |
|:--------------------------------|:---------------------|:---------------------------------|
| connectTimeoutMs                | 0 (infinite)         | 10,000                           |
| readTimeoutMs                   | 0 (infinite)         | 10,000                           |
| blocking                        | true                 |                                  |
| bufferSize                      | 1024                 |                                  |
| shutdownTimeout                 | 0 (wait until empty) | 30,000 (ms)                      |
| asyncQueueFullPolicy            | [`Default`](https://logging.apache.org/log4j/2.x/javadoc/log4j-core/org/apache/logging/log4j/core/async/DefaultAsyncQueueFullPolicy.html) | [`CustomDiscardingAsyncQueueFullPolicy`](src/main/java/com/dsingley/log4j2elk/CustomDiscardingAsyncQueueFullPolicy.java) |
| discardThreshold                | INFO                 |                                  |

<sup>*</sup> corresponding environment variables are prefixed with `ELK_` and separated by `_`, e.g. `ELK_CONNECT_TIMEOUT_MS`
