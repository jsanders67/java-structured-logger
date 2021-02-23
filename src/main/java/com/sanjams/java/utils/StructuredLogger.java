package com.sanjams.java.utils;

import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.JsonLayout;
import org.slf4j.Logger;

/**
 * This class is a wrapper on a Slf4j logger. It writes logs in a structured json format for better
 * parsability. It can be dropped in along with your existing Slf4j logger and not muddle with any existing log
 * structure. It strives to be like golang's logrus (https://github.com/sirupsen/logrus).
 *
 * Current Limitations:
 * * Logged structures will be sub-fields of the "message" field in the final log output. This is because
 *   this class uses the log4j2 "objectMessageAsJsonObject" property to enable structured formatting. Example:
 *   {
 *     "level": "INFO",
 *     "timestamp": "...",
 *     "message": {
 *       "biz": {
 *         ...
 *       },
 *       "foo": "bar"
 *     }
 *   }
 * * The loggerName will be "my.random.class.path.utils.logging.StructuredLogger" however to account
 *   for this, a field is added to the structure for "parentLoggerName" which will be that of the wrapped Slf4j logger
 * * Settings of the CLASS_LOGGER used to write out messages are not configurable however this can be updated in the
 *   future.
 * * This implementation sacrifices additional memory usage for readability and simplicity. Mainly new
 *   objects are created on each "withContext" call.
 */
public class StructuredLogger {

  private static final org.apache.logging.log4j.Logger DEFAULT_CLASS_LOGGER = setupClassLogger();

  private static org.apache.logging.log4j.Logger setupClassLogger() {
    LoggerContext context = (LoggerContext) LogManager.getContext();
    Configuration config = context.getConfiguration();
    JsonLayout layout = JsonLayout.newBuilder()
        .setCompact(true)
        .setEventEol(true)
        .setProperties(true)
        .setObjectMessageAsJsonObject(true)
        .build();
    Appender appender = ConsoleAppender.newBuilder()
        .setName("Structured")
        .setLayout(layout)
        .build();
    appender.start();
    AppenderRef refs = AppenderRef.createAppenderRef("Structured", null, null);
    LoggerConfig loggerConfig = LoggerConfig.createLogger(
        false,
        Level.ALL,
        StructuredLogger.class.getName(),
        null,
        new AppenderRef[]{ refs },
        null,
        config,
        null);
    loggerConfig.addAppender(appender, null, null);
    config.addLogger(StructuredLogger.class.getName(), loggerConfig);
    return context.getLogger(StructuredLogger.class.getName());
  }

  private final Logger logger;
  private final org.apache.logging.log4j.Logger classLogger;
  private final Map<String, Object> context;

  public StructuredLogger(final Logger logger) {
    if (logger == null) {
      throw new IllegalArgumentException("logger cannot be null");
    }
    this.logger = logger;
    this.classLogger = DEFAULT_CLASS_LOGGER;
    this.context = new TreeMap<>();
  }

  StructuredLogger(final Logger logger, org.apache.logging.log4j.Logger classLogger) {
    this.logger = logger;
    this.classLogger = classLogger;
    this.context = new TreeMap<>();
  }

  /**
   * Creates a new logger with the context of the current logger while also adding the new specified
   * keys to the context. This is useful when you want to have multiple
   *
   * @param name key name for context object
   * @param value object to add to context
   * @return new StructuredLogger
   */
  public StructuredLogger withContext(String name, Object value) {
    StructuredLogger logger = new StructuredLogger(this.logger, this.classLogger);
    logger.context.putAll(this.context);
    logger.context.put(name, value);
    return logger;
  }

  // TRACE

  public void trace(String message) {
    if (!logger.isTraceEnabled()) {
      return;
    }
    log(Level.TRACE, message);
  }

  // DEBUG

  public void debug(String message) {
    if (!logger.isDebugEnabled()) {
      return;
    }
    log(Level.DEBUG, message);
  }

  // INFO

  public void info(String message) {
    if (!logger.isInfoEnabled()) {
      return;
    }
    log(Level.INFO, message);
  }

  // WARN

  public void warn(String message) {
    if (!logger.isWarnEnabled()) {
      return;
    }
    log(Level.WARN, message);
  }

  // ERROR

  public void error(String message) {
    if (!logger.isErrorEnabled()) {
      return;
    }
    log(Level.ERROR, message);
  }

  private void log(Level level, String message) {
    Map<String, Object> msg = new TreeMap<>();
    msg.put("message", message);
    msg.put("parentLoggerName", logger.getName());
    msg.putAll(this.context);
    classLogger.log(level, msg);
  }
  
}
