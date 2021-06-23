package plus.extvos.mqtt.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;
import plus.extvos.mqtt.convert.BodyDeserialize;
import plus.extvos.mqtt.convert.BodySerialize;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * default mqtt payload config for jackson.
 *
 * @author tocrhz
 */
@Order(1001)
@AutoConfigureAfter({JacksonAutoConfiguration.class})
@ConditionalOnClass(ObjectMapper.class)
@Configuration
public class PayloadJacksonAutoConfiguration {
    private final static Logger log = LoggerFactory.getLogger(PayloadJacksonAutoConfiguration.class);

    @Bean
    @Order(1001)
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new MqttDefaultJacksonModule());
        return objectMapper;
    }

    @Bean
    @Order(1002)
    @ConditionalOnMissingBean(BodySerialize.class)
    public BodySerialize payloadSerialize(ObjectMapper objectMapper) {
        return source -> {
            try {
                return objectMapper.writeValueAsBytes(source);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("Payload serialize error: {}", e.getMessage(), e);
            }
            return null;
        };
    }

    @Bean
    @Order(1002)
    @ConditionalOnMissingBean(BodyDeserialize.class)
    public BodyDeserialize payloadDeserialize(ObjectMapper objectMapper) {
        return new BodyDeserialize() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> Converter<byte[], T> getConverter(Class<T> targetType) {
                return source -> {
                    try {
                        if (targetType == String.class) {
                            return (T) new String(source, StandardCharsets.UTF_8);
                        }
                        return objectMapper.readValue(source, targetType);
                    } catch (IOException e) {
                        log.warn("Payload deserialize error: {}", e.getMessage(), e);
                    }
                    return null;
                };
            }
        };
    }

    public static class MqttDefaultJacksonModule extends SimpleModule {
        public static final Version VERSION = VersionUtil.parseVersion("1.1.0",
                "io.github.extvos",
                "quick-lib-mqtt");

        private final static ZoneId ZONE_ID = ZoneId.of("GMT+8");
        private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

        public MqttDefaultJacksonModule() {
            super(VERSION);

            addSerializer(LocalDateTime.class, LOCAL_DATE_TIME_JSON_SERIALIZER);
            addSerializer(LocalDate.class, LOCAL_DATE_JSON_SERIALIZER);
            addSerializer(LocalTime.class, LOCAL_TIME_JSON_SERIALIZER);
            addSerializer(Date.class, DATE_JSON_SERIALIZER);

            addDeserializer(LocalDateTime.class, LOCAL_DATE_TIME_JSON_DESERIALIZER);
            addDeserializer(LocalDate.class, LOCAL_DATE_JSON_DESERIALIZER);
            addDeserializer(LocalTime.class, LOCAL_TIME_JSON_DESERIALIZER);
            addDeserializer(Date.class, DATE_JSON_DESERIALIZER);
        }

        private final static JsonSerializer<LocalDateTime> LOCAL_DATE_TIME_JSON_SERIALIZER = new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(value.atZone(ZONE_ID).format(DATE_TIME_FORMATTER));
                }
            }
        };
        private final static JsonSerializer<LocalDate> LOCAL_DATE_JSON_SERIALIZER = new JsonSerializer<LocalDate>() {

            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(value.format(DATE_FORMATTER));
                }
            }
        };
        private final static JsonSerializer<LocalTime> LOCAL_TIME_JSON_SERIALIZER = new JsonSerializer<LocalTime>() {

            @Override
            public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(value.format(TIME_FORMATTER));
                }
            }
        };
        private final static JsonSerializer<Date> DATE_JSON_SERIALIZER = new JsonSerializer<Date>() {

            @Override
            public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(DATE_TIME_FORMATTER.format(value.toInstant().atZone(ZONE_ID)));
                }
            }
        };

        private final static JsonDeserializer<LocalDateTime> LOCAL_DATE_TIME_JSON_DESERIALIZER = new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getValueAsString();
                if (StringUtils.hasLength(value)) {
                    return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
                }
                return null;
            }
        };
        private final static JsonDeserializer<LocalDate> LOCAL_DATE_JSON_DESERIALIZER = new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getValueAsString();
                if (StringUtils.hasLength(value)) {
                    return LocalDate.parse(value, DATE_FORMATTER);
                }
                return null;
            }
        };
        private final static JsonDeserializer<LocalTime> LOCAL_TIME_JSON_DESERIALIZER = new JsonDeserializer<LocalTime>() {
            @Override
            public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getValueAsString();
                if (StringUtils.hasLength(value)) {
                    return LocalTime.parse(value, TIME_FORMATTER);
                }
                return null;
            }
        };
        private final static JsonDeserializer<Date> DATE_JSON_DESERIALIZER = new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getValueAsString();
                if (StringUtils.hasLength(value)) {
                    return Date.from(LocalDateTime.parse(value, DATE_TIME_FORMATTER).atZone(ZONE_ID).toInstant());
                }
                return null;
            }
        };

    }
}