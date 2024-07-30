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

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ContentType;
import dev.langchain4j.data.message.ImageContent;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Content interface makes the serialization more complex than it should be.
 * This flatten structure is a workaround to simplify the serialization.
 */
@Data
@NoArgsConstructor
public class AstraDbContent implements Content  {

    private String text;

    private ContentType type;

    private Image image;

    private ImageContent.DetailLevel detailLevel;

    public AstraDbContent(Content c) {
        switch(c.type()) {
            case TEXT:
                this.text = ((dev.langchain4j.data.message.TextContent) c).text();
                this.type = ContentType.TEXT;
                break;
            case IMAGE:
                this.image = ((ImageContent) c).image();
                this.detailLevel = ((ImageContent) c).detailLevel();
                this.type = ContentType.IMAGE;
                break;
        }
    }

    public Content asContent() {
        switch (type) {
            case TEXT:
                return new dev.langchain4j.data.message.TextContent(text);
            case IMAGE:
                return new ImageContent(image, detailLevel);
            default:
                throw new IllegalStateException("Unknown content type: " + type);
        }
    }

    @Override
    public ContentType type() {
        return type;
    }

}
