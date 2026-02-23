package com.dekra.service.foundation.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ch.qos.logback.classic.Logger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LoggerServiceTest {

    private Logger logger;
    private Appender<ILoggingEvent> mockAppender;
    private ArgumentCaptor<ILoggingEvent> logCaptor;

    @BeforeEach
    void setUp() {
        logger = (Logger) org.slf4j.LoggerFactory.getLogger(LoggerService.class);
        mockAppender = mock(Appender.class);
        logger.setLevel(Level.DEBUG);
        logger.addAppender(mockAppender);
        logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
    }

    @Test
    void testInfoLog() {
        LoggerService.info("Test info message");

        verify(mockAppender, times(1)).doAppend(logCaptor.capture());
        ILoggingEvent logEvent = logCaptor.getValue();

        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("Test info message", logEvent.getFormattedMessage());
    }

    @Test
    void testErrorLog() {
        LoggerService.error("Test error message");

        verify(mockAppender, times(1)).doAppend(logCaptor.capture());
        ILoggingEvent logEvent = logCaptor.getValue();

        assertEquals(Level.ERROR, logEvent.getLevel());
        assertEquals("Test error message", logEvent.getFormattedMessage());
    }

    @Test
    void testWarnLog() {
        LoggerService.warn("Test warn message");

        verify(mockAppender, times(1)).doAppend(logCaptor.capture());
        ILoggingEvent logEvent = logCaptor.getValue();

        assertEquals(Level.WARN, logEvent.getLevel());
        assertEquals("Test warn message", logEvent.getFormattedMessage());
    }

    @Test
    void testDebugLog() {
        LoggerService.debug("Test debug message");

        verify(mockAppender, times(1)).doAppend(logCaptor.capture());
        ILoggingEvent logEvent = logCaptor.getValue();

        assertEquals(Level.DEBUG, logEvent.getLevel());
        assertEquals("Test debug message", logEvent.getFormattedMessage());
    }
}
