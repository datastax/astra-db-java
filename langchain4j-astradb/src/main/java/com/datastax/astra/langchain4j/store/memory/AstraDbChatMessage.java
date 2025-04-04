package com.datastax.astra.langchain4j.store.memory;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A chat message stored in AstraDB.
 */
@NoArgsConstructor
@Setter @Getter
@Accessors(fluent = true)
public class AstraDbChatMessage implements ChatMessage {

    /** Public Static to help build filters if any. */
    public static final String PROP_CHAT_ID = "chat_id";

    /** Public Static to help build filters if any. */
    public static final String PROP_MESSAGE = "text";

    /** Public Static to help build filters if any. */
    public static final String PROP_MESSAGE_ID = "message_id";

    /** Public Static to help build filters if any. */
    public static final String PROP_MESSAGE_TIME = "message_time";

    /** Public Static to help build filters if any. */
    public static final String PROP_MESSAGE_TYPE = "message_type";

    /** Public Static to help build filters if any. */
    public static final String PROP_TOOLS = "tools_execution_requests";

    /** Public Static to help build filters if any. */
    public static final String PROP_CONTENTS = "contents";

    /** Public Static to help build filters if any. */
    public static final String PROP_NAME = "name";

    @JsonProperty(PROP_CHAT_ID)
    private String chatId;

    @JsonProperty(PROP_MESSAGE_ID)
    private UUID messageId;

    @JsonProperty(PROP_MESSAGE_TYPE)
    private ChatMessageType messageType;

    @JsonProperty(PROP_MESSAGE_TIME)
    private Instant messageTime;

    @JsonProperty(PROP_NAME)
    private String name;

    @JsonProperty(PROP_MESSAGE)
    private String text;

    @JsonProperty(PROP_TOOLS)
    private List<ToolExecutionRequest> toolExecutionRequests;

    @JsonProperty(PROP_CONTENTS)
    private List<AstraDbContent> contents;

    /** {@inheritDoc} */
    @Override
    public ChatMessageType type() {
        return messageType;
    }

    @Data @NoArgsConstructor
    public static class ToolExecutionRequest {
        private String id;
        private String name;
        private String arguments;
        public ToolExecutionRequest(dev.langchain4j.agent.tool.ToolExecutionRequest lc4jTER) {
            this.id = lc4jTER.id();
            this.name = lc4jTER.name();
            this.arguments = lc4jTER.arguments();
        }
        public dev.langchain4j.agent.tool.ToolExecutionRequest asLc4j() {
            return dev.langchain4j.agent.tool.ToolExecutionRequest.builder()
                    .id(this.id)
                    .name(this.name)
                    .arguments(this.arguments)
                    .build();
        }
    }

    /**
     * Constructor using the Langchain4J abstraction
     * @param chatMessage
     *      lc4j classes
     */
    public AstraDbChatMessage(ChatMessage chatMessage) {
        this.messageType = chatMessage.type();
        this.messageTime = Instant.now();
        // Flatten to ease serialization in DB
        switch (chatMessage.type()) {
            case SYSTEM:
                SystemMessage systemMessage = (SystemMessage) chatMessage;
                this.text = systemMessage.text();
            break;
            case USER:
                UserMessage userMessage = (UserMessage) chatMessage;
                this.name  = userMessage.name();
                if (userMessage.contents() != null) {
                    this.contents = userMessage.contents()
                            .stream()
                            .map(AstraDbContent::new).collect(Collectors.toList());
                    if (!this.contents.isEmpty()) {
                        this.text = this.contents.get(0).getText();
                    }
                }
                break;
            case AI:
                AiMessage aiMessage = (AiMessage) chatMessage;
                this.text = aiMessage.text();
                if (aiMessage.toolExecutionRequests() != null) {
                    this.toolExecutionRequests = aiMessage.toolExecutionRequests()
                            .stream().map(ToolExecutionRequest::new)
                            .collect(Collectors.toList());
                }
                break;
            case TOOL_EXECUTION_RESULT:
                ToolExecutionResultMessage tes = (ToolExecutionResultMessage) chatMessage;
                this.name = tes.id() + tes.toolName();
                this.text = tes.text();
            break;
            default:
                throw new IllegalArgumentException("Unknown message type: " + chatMessage.type());
        }
    }

    /**
     * Downcast to {@link ChatMessage}.
     *
     * @return
     *      chatMessage interface
     */
    public ChatMessage toChatMessage() {
        switch (messageType) {
            case SYSTEM:
                return new SystemMessage(text);
            case USER:
                List<Content> targetContents = new ArrayList<>();
                if (this.contents!=null) {
                    targetContents.addAll(contents.stream()
                            .map(AstraDbContent::asContent)
                            .collect(Collectors.toList()));
                }
                if (name != null) {
                    return new UserMessage(name, targetContents);
                }
                return new UserMessage(targetContents);
            case AI:
                if (this.toolExecutionRequests != null) {
                    List< dev.langchain4j.agent.tool.ToolExecutionRequest> request = this.toolExecutionRequests
                            .stream().map(ToolExecutionRequest::asLc4j)
                            .collect(Collectors.toList());
                    if (text == null) {
                        return new AiMessage(request);
                    }
                    return new AiMessage(text, request);
                }
                return new AiMessage(text);
            case TOOL_EXECUTION_RESULT:
                if (this.toolExecutionRequests != null) {
                    List<dev.langchain4j.agent.tool.ToolExecutionRequest> request = this.toolExecutionRequests
                            .stream().map(ToolExecutionRequest::asLc4j)
                            .collect(Collectors.toList());
                    if (!request.isEmpty()) {
                        dev.langchain4j.agent.tool.ToolExecutionRequest tool = request.get(0);
                        return new ToolExecutionResultMessage(tool.id(), tool.name(), text);
                    }
                }
                return new ToolExecutionResultMessage(null, null, text);
            default:
                throw new IllegalArgumentException("Unknown message type: " + messageType);
        }
    }
}
