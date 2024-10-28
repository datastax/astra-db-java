package com.datastax.astra.internal.command;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.internal.api.ApiData;
import com.datastax.astra.internal.api.ApiError;
import com.datastax.astra.internal.utils.AnsiUtils;
import com.datastax.astra.internal.serializer.collections.DocumentSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.List;
import java.util.UUID;

/**
 * Implements a {@link CommandObserver} that logs command execution details. This observer uses SLF4J for logging,
 * providing flexibility to integrate with various logging frameworks (e.g., Logback, Log4J). The logging level and
 * the source class for logging can be customized, allowing for fine-grained control over the log output.
 */
public class LoggingCommandObserver implements CommandObserver {

    /**
     * The logger instance used to log command execution details. This logger is configured based on the source class
     * provided during initialization, allowing log messages to be correctly associated with the part of the application
     * that initiated the command execution.
     */
    private final Logger logger;

    /**
     * The logging level at which command execution details should be logged. This level can be dynamically set to control
     * the verbosity of the log output, making it easier to filter logs based on severity or importance.
     */
    private final Level logLevel;

    /**
     * Initializes a new {@code LoggingCommandObserver} instance with a default logging level of DEBUG. This constructor
     * is convenient when a moderate level of logging detail is sufficient, and it associates the logging output with the
     * specified source class.
     *
     * @param sourceClass The class from which the logging will be performed. This parameter is used to initialize the logger
     *                    and associate log messages with the correct part of the application.
     */
    public LoggingCommandObserver(Class<?> sourceClass) {
        this(Level.DEBUG, sourceClass);
    }

    /**
     * Initializes a new {@code LoggingCommandObserver} instance with a specified logging level and source class. This constructor
     * offers full control over the logging configuration, allowing for detailed customization of the logging behavior.
     *
     * @param logLevel    The logging level to use for logging command execution details. This level determines the verbosity of the
     *                    log output.
     * @param sourceClass The class from which the logging will be performed. This parameter is used to initialize the logger
     *                    and ensure that log messages are correctly categorized in the application's log output.
     */
    public LoggingCommandObserver(Level logLevel, Class<?> sourceClass) {
        this.logLevel = logLevel;
        this.logger = LoggerFactory.getLogger(sourceClass);
    }

    /** {@inheritDoc} */
    @Override
    public void onCommand(ExecutionInfos executionInfo) {
        if (executionInfo != null) {
            String req = UUID.randomUUID().toString().substring(30);
            // Log Command
            log("Command [" + AnsiUtils.cyan(executionInfo.getCommand().getName()) + "] with id [" + AnsiUtils.cyan(req) + "]");
            log(AnsiUtils.magenta("[" + req + "][url]") + "=" +
                    AnsiUtils.yellow("{}"), executionInfo.getRequestUrl());
            log(AnsiUtils.magenta("[" + req + "][request]") + "=" + AnsiUtils.yellow("{}"),
                    executionInfo.getSerializer().marshall(executionInfo.getCommand()));
            log(AnsiUtils.magenta("[" + req + "][response-code]") + "=" + AnsiUtils.yellow("{}"),
                    executionInfo.getResponseHttpCode());
            log(AnsiUtils.magenta("[" + req + "][response-body]") + "=" + AnsiUtils.yellow("{}"),
                    executionInfo.getSerializer().marshall(executionInfo.getResponse()));
            log(AnsiUtils.magenta("[" + req + "][response-time]") + "=" + AnsiUtils.yellow("{}") + " millis.",
                    executionInfo.getExecutionTime());
            // Log Data
            ApiData data = executionInfo.getResponse().getData();
            if (data != null && data.getDocument() != null) {
                log(AnsiUtils.magenta("[" + req + "][apiData/document]") + "=" + AnsiUtils.yellow("1 document retrieved, id='{}'"), data.getDocument().getId(Object.class));
            }
            if (data != null && data.getDocuments() != null) {
                log(AnsiUtils.magenta("[" + req + "][apiData/documents]") + "=" + AnsiUtils.yellow("{} document(s)."), data.getDocuments().size());
            }

            // Log Errors
            List<ApiError> errors = executionInfo.getResponse().getErrors();
            if (errors != null) {
                log(AnsiUtils.magenta("[" + req + "][errors]") + "="+ AnsiUtils.yellow("{}") +" errors detected.", errors.size());
                for (ApiError error : errors) {
                    log(AnsiUtils.magenta("[" + req + "][errors]")+ "="+ AnsiUtils.yellow("{} [code={}]"), error.getErrorMessage(), error.getErrorCode());
                }
            }
        }
    }

    /**
     * Convenient method to adjust dynamically the log level.
     * @param message
     *      log message
     * @param params
     *      arguments for the log message.
     */
    public void log(String message, Object... params) {
        switch (this.logLevel) {
            case TRACE:
                logger.trace(message, params);
                break;
            case DEBUG:
                logger.debug(message, params);
                break;
            case INFO:
                logger.info(message, params);
                break;
            case WARN:
                logger.warn(message, params);
                break;
            case ERROR:
                logger.error(message, params);
                break;
        }
    }
}
