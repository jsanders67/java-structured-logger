## Java Structured Logger

[![Build Status](https://travis-ci.com/jsanders67/java-structured-logger.svg?branch=master)](https://travis-ci.com/jsanders67/java-structured-logger)

### Overview

This is a simple helper utility for producing structured logs when using Sl4j. The problem it solves is the lack of structured logging provided by the [JSONLayout](https://logging.apache.org/log4j/2.x/manual/layouts.html#JSONLayout) in Log4j. By default, the JSONLayout does not structure the log message. This can be achieved by setting the "objectMessageAsJsonObject" property on the layout, but then all logs are structured and the ability for string interpolation is lost. MDC is another alternative, but it does not allow for structuring complex objects, only strings fields and values. This project solves these problems by providing a wrapper for your Slf4j logger that one can use to output json structured logs when desired. Its inspiration comes from the golang package [logrus](https://github.com/sirupsen/logrus)

### Usage
Declare a dependency in your `pom.xml`. Im currently not quite sure if this will work as is. If it doesnt please raise an issue and I will fix it.
```xml
<dependency>
  <groupId>com.github.jsanders67</groupId>
  <artifactId>java-structured-logger</artifactId>
  <version>1.0</version>
</dependency>
```

Example code:

```java
Logger slf4jLogger = LoggerFactory.getLogger("Sfl4jLogger");
StructuredLogger structuredLogger = new StructuredLogger(Sfl4jLogger);
structuredLogger.withContext("foo", "bar").info("message"); 
```
This would output

```json
{
   "level": "INFO",
   "timestamp": "...",
   "message": {
     "foo": "bar",
     "message": "message"
   }
}
```

Complex objects can also be logged and structured. Example:

```java
Item iAmAComplexObject = new Item("field1", new Item("innerItemField"), 42);
structuredLogger.withContext("complexObject", iAmAComplexObject).info("this line is complex!"); 
```

Outputs:

```json
output:
{
   "level": "INFO",
   "timestamp": "...",
   "message": {
     "complexObject": {
       "field1": "field1",
       "field2": {
         "field1": "innerItemField"
       },
       "field3": 42
     },
     "message": "this line is complex!"
   }
}
```

The Structured logger will only log at the level of the wrapped Slf4j logger. So if the Sl4j logger's level is `INFO`, the following would produce no output

```java
structuredLogger.withContext("foo", "bar").debug("i wont show up :)"); 
```

StructuredLoggers produced by the `withContext()` method persist the context for later. Example:

```java
...
StructuredLogger logger = structuredLogger.withContext("foo", "bar");
logger.info("message1");
logger.info("message2");
```

Outputs:

```json
{
   "level": "INFO",
   "timestamp": "...",
   "message": {
     "foo": "bar",
     "message": "message1"
   }
}
{
   "level": "INFO",
   "timestamp": "...",
   "message": {
     "foo": "bar",
     "message": "message2"
   }
}
```

NOTE: outputs above are nicely formatted for readability. In practice these logs are emitted on one line in jsonl file format.


### Limitations
* Currently this only works for Slf4j configured with Log4j.
* Logged structures will be sub-fields of the "message" field in the final log output. This is because
  this class uses the log4j2 "objectMessageAsJsonObject" property to enable structured formatting. Example:
  ```json
  {
    "level": "INFO",
    "timestamp": "...",
    "message": {
      "biz": {
        ...
      },
      "foo": "bar"
    }
  }
  ```
* The loggerName will be "my.random.class.path.utils.logging.StructuredLogger" however to account
  for this, a field is added to the structure for "parentLoggerName" which will be that of the wrapped Slf4j logger
* Settings of the CLASS_LOGGER used to write out messages are not configurable however this can be updated in the
  future.
* This implementation sacrifices additional memory usage for readability and simplicity. Mainly new
  objects are created on each "withContext" call.

### Notes
You can tell that Im currently not familiar with the process for packaging and publishing java packages so this is a learning experience for me