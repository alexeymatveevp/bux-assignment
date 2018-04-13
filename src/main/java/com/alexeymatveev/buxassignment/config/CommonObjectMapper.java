package com.alexeymatveev.buxassignment.config;

import com.alexeymatveev.buxassignment.model.message.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

/**
 * Customized object mapper.
 *
 * Created by Alexey Matveev on 4/10/2018.
 */
public class CommonObjectMapper extends ObjectMapper {

    public CommonObjectMapper() {
        // basic config
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // java time deser. module
        this.registerModule(new JavaTimeModule());

        // custom deser. module
        SimpleModule customDeserializersModule = new SimpleModule();
        customDeserializersModule.addDeserializer(BaseTMsg.class, new BaseTMsgDeserializer());
        this.registerModule(customDeserializersModule);
    }

    /**
     * Deserializer for web socket T-based messages.
     */
    class BaseTMsgDeserializer extends StdDeserializer<BaseTMsg> {

        BaseTMsgDeserializer() {
            this(null);
        }

        private BaseTMsgDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public BaseTMsg deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            String t = node.get("t").asText();
            MsgType type = MsgType.parse(t);
            if (type != MsgType.UNKNOWN) {
                // validate type with actual body
                JsonNode data = node.get("body");
                if (validateTypeBody(data, type)) {
                    // deserialize actual body
                    BodyMsg bodyMsg;
                    switch (type) {
                        case CONNECT_FAILED:
                            bodyMsg = treeToValue(data, ErrorMsg.class);
                            break;
                        case CONNECT_CONNECTED:
                            // not interested in body right now
                            bodyMsg = new EmptyMsg();
                            break;
                        case TRADING_QUOTE:
                            bodyMsg = treeToValue(data, TradingQuoteMsg.class);
                            break;
                        default:
                            throw new JsonParseException(p, "Unknown message type: " + type);
                    }
                    return new BaseTMsg<>(type, bodyMsg);
                } else {
                    throw new JsonParseException(p, "Cannot match message body for type " + type);
                }
            }
            return new BaseTMsg<>(MsgType.UNKNOWN, null);
        }

        boolean validateTypeBody(JsonNode node, MsgType type) {
            switch (type) {
                case CONNECT_CONNECTED:
                    return true;
                case CONNECT_FAILED:
                    return isErrorMsg(node);
                case TRADING_QUOTE:
                    return node.get("securityId") != null && node.get("currentPrice") != null;
                default:
                    return false;
            }
        }

        boolean isErrorMsg(JsonNode node) {
            return node.get("developerMsg") != null && node.get("errorCode") != null;
        }

    }
}
