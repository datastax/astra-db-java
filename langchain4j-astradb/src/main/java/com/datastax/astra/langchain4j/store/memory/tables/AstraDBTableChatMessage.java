package com.datastax.astra.langchain4j.store.memory.tables;

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

import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.langchain4j.store.memory.AstraDbChatMessage;
import com.datastax.astra.langchain4j.store.memory.AstraDbContent;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@EntityTable
public class AstraDBTableChatMessage {

    @Column(name="chat_id")
    UUID chatId;

    @Column(name="message_id")
    UUID messageId;

    @Column(name="message_type")
    String messageType;

    @Column(name="message_time")
    Instant messageTime;

    @Column(name="text")
    String text;

    @Column(name="name")
    String name;

    @Column(name="contents")
    Map<String, String> contents = new HashMap<>();

    @Column(name="tools_arguments")
    Map<String, String> toolsArguments = new HashMap<>();

    @Column(name="tools_name")
    Map<String, String> toolsName = new HashMap<>();

    public AstraDBTableChatMessage chatId(UUID chatId) {
        this.chatId = chatId;
        return this;
    }

    /**
     * Constructor using the Langchain4J abstraction
     * @param chatMessage
     *      lc4j classes
     */
    public AstraDBTableChatMessage(ChatMessage chatMessage) {
        this.messageId   = UUID.randomUUID();
        this.messageType = chatMessage.type().name();
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
                    userMessage
                      .contents().stream()
                      .map(AstraDbContent::new)
                      .forEach(content -> {
                         this.contents.put(content.getType().name(), content.getText());
                         this.text = content.getText();
                      });
                }
                break;
            case AI:
                AiMessage aiMessage = (AiMessage) chatMessage;
                this.text = aiMessage.text();
                if (aiMessage.toolExecutionRequests() != null) {
                    aiMessage
                            .toolExecutionRequests()
                            .stream().map(AstraDbChatMessage.ToolExecutionRequest::new)
                            .forEach(ter -> {
                                this.toolsArguments.put(ter.id(), ter.arguments());
                                this.toolsName.put(ter.id(), ter.name());
                            });
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
            case "SYSTEM":
                return new SystemMessage(text);
            case "USER":
                List<Content> targetContents = new ArrayList<>();
                if (this.contents!=null) {
                    this.text = this.contents.get("TEXT");
                }
                if (name != null) {
                    return new UserMessage(name, targetContents);
                }
                return new UserMessage(targetContents);
            case "AI":
                //if (this.toolExecutionRequests != null) {
                //    List< dev.langchain4j.agent.tool.ToolExecutionRequest> request = this.toolExecutionRequests
                //            .stream().map(AstraDbChatMessage.ToolExecutionRequest::asLc4j)
                //            .collect(Collectors.toList());
                //    if (text == null) {
                //        return new AiMessage(request);
                //    }
                //    return new AiMessage(text, request);
                //}
                return new AiMessage(text);
            case "TOOL_EXECUTION_RESULT":
               // if (this.toolExecutionRequests != null) {
               //     List<dev.langchain4j.agent.tool.ToolExecutionRequest> request = this.toolExecutionRequests
               //             .stream().map(AstraDbChatMessage.ToolExecutionRequest::asLc4j)
               //             .collect(Collectors.toList());
               //     if (!request.isEmpty()) {
               //         dev.langchain4j.agent.tool.ToolExecutionRequest tool = request.get(0);
               //         return new ToolExecutionResultMessage(tool.id(), tool.name(), text);
               //     }
               // }
                return new ToolExecutionResultMessage(null, null, text);
            default:
                throw new IllegalArgumentException("Unknown message type: " + messageType);
        }
    }

}
