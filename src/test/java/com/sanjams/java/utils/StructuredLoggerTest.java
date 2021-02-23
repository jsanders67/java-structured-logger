package com.sanjams.java.utils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.Level;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StructuredLoggerTest {

  private static class Item {
    private String name;
    private int count;
  }

  private static final Item DUMMY_ITEM = new Item();
  static {
    DUMMY_ITEM.count = 42;
    DUMMY_ITEM.name = "foo";
  }

  @Mock
  private Logger slf4jLogger;

  @Mock
  private org.apache.logging.log4j.Logger classLogger;

  private StructuredLogger slogger;

  @BeforeMethod
  public void beforeMethod() {
    MockitoAnnotations.initMocks(this);
    slogger = new StructuredLogger(slf4jLogger, classLogger);
    when(slf4jLogger.getName()).thenReturn("ParentLoggerName");
  }

  @Test
  public void testDefaultConstructor_doesNotBarf() {
    doReturn(true).when(slf4jLogger).isInfoEnabled();
    StructuredLogger logger = new StructuredLogger(slf4jLogger);
    logger.withContext("item", DUMMY_ITEM).info("message");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDefaultConstructor_withNullLogger_throwsIllegalArgumentException() {
    new StructuredLogger(null);
  }

  @Test
  public void testTrace_WhenEnabled_addsContext() {
    doReturn(true).when(slf4jLogger).isTraceEnabled();
    slogger.withContext("item", DUMMY_ITEM).trace("message");
    verify(classLogger, times(1)).log(Level.TRACE, getExpectedMessage("message"));
  }

  @Test
  public void testTrace_WhenNotEnabled_doesNothing() {
    doReturn(false).when(slf4jLogger).isTraceEnabled();
    slogger.withContext("item", DUMMY_ITEM).trace("message");
    verifyZeroInteractions(classLogger);
  }

  @Test
  public void testDebug_WhenEnabled_addsContext() {
    doReturn(true).when(slf4jLogger).isDebugEnabled();
    slogger.withContext("item", DUMMY_ITEM).debug("message");
    verify(classLogger, times(1)).log(Level.DEBUG, getExpectedMessage("message"));
  }

  @Test
  public void testDebug_WhenNotEnabled_doesNothing() {
    doReturn(false).when(slf4jLogger).isDebugEnabled();
    slogger.withContext("item", DUMMY_ITEM).debug("message");
    verifyZeroInteractions(classLogger);
  }

  @Test
  public void testInfo_WhenEnabled_addsContext() {
    doReturn(true).when(slf4jLogger).isInfoEnabled();
    slogger.withContext("item", DUMMY_ITEM).info("message");
    verify(classLogger, times(1)).log(Level.INFO, getExpectedMessage("message"));
  }

  @Test
  public void testInfo_WhenNotEnabled_doesNothing() {
    doReturn(false).when(slf4jLogger).isInfoEnabled();
    slogger.withContext("item", DUMMY_ITEM).info("message");
    verifyZeroInteractions(classLogger);
  }

  @Test
  public void testWarn_WhenEnabled_addsContext() {
    doReturn(true).when(slf4jLogger).isWarnEnabled();
    slogger.withContext("item", DUMMY_ITEM).warn("message");
    verify(classLogger, times(1)).log(Level.WARN, getExpectedMessage("message"));
  }

  @Test
  public void testWarn_WhenNotEnabled_doesNothing() {
    doReturn(false).when(slf4jLogger).isWarnEnabled();
    slogger.withContext("item", DUMMY_ITEM).warn("message");
    verifyZeroInteractions(classLogger);
  }

  @Test
  public void testError_WhenEnabled_addsContext() {
    doReturn(true).when(slf4jLogger).isErrorEnabled();
    slogger.withContext("item", DUMMY_ITEM).error("message");
    verify(classLogger, times(1)).log(Level.ERROR, getExpectedMessage("message"));
  }

  @Test
  public void testError_WhenNotEnabled_doesNothing() {
    doReturn(false).when(slf4jLogger).isErrorEnabled();
    slogger.withContext("item", DUMMY_ITEM).error("message");
    verifyZeroInteractions(classLogger);
  }

  @Test
  public void testWithContext_returnsLoggerThatMaintainsContext() {
    doReturn(true).when(slf4jLogger).isInfoEnabled();
    StructuredLogger newLogger = slogger.withContext("item", DUMMY_ITEM);
    newLogger.info("message1");
    newLogger.info("message2");
    verify(classLogger, times(1)).log(Level.INFO, getExpectedMessage("message1"));
    verify(classLogger, times(1)).log(Level.INFO, getExpectedMessage("message2"));
  }

  @Test
  public void testWithContext_whenCalledTwice_hasBothContexts() {
    doReturn(true).when(slf4jLogger).isInfoEnabled();
    Item item2 = new Item();
    item2.count = 63;
    item2.name = "bar";
    slogger.withContext("item1", DUMMY_ITEM)
        .withContext("item2", item2)
        .info("message");
    Map<String, Object> expectedMessage = new TreeMap<>();
    expectedMessage.put("message", "message");
    expectedMessage.put("item1", DUMMY_ITEM);
    expectedMessage.put("item2", item2);
    expectedMessage.put("parentLoggerName", "ParentLoggerName");
    verify(classLogger, times(1)).log(Level.INFO, expectedMessage);
  }

  private Map<String, Object> getExpectedMessage(String message) {
    Map<String, Object> expectedMessage = new TreeMap<>();
    expectedMessage.put("item", DUMMY_ITEM);
    expectedMessage.put("message", message);
    expectedMessage.put("parentLoggerName", "ParentLoggerName");
    return expectedMessage;
  }
}
