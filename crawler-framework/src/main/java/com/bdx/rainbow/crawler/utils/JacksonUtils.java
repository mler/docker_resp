package com.bdx.rainbow.crawler.utils;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:stxpons@gmail.com">stxpons@gmail.com</a>
 * @version V1.0.0
 * 
 * @author mler
 *
 */
public final class JacksonUtils {
    private JacksonUtils() {
    }

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    /**
     * 一些代码使用了 new ObjectMapper,没次new的代价需要15毫秒左右，改成指向该单例对象了
     *
     * @return ObjectMapper，单例
     */
    @Deprecated
    public static ObjectMapper getMapperInstance() {
        return mapper;
    }

    public static String toJson(final Object bean) throws IOException {
        return mapper.writeValueAsString(bean);
    }

    public static <T> T toBean(final String json, final Class<T> clazz) throws IOException {
        return toBean(json, clazz, false);
    }

    public static <T> T toBean(final String json, final Class<T> clazz, final boolean isCache) throws IOException {
        ObjectMapper objectMapper;
        if (isCache) {
            objectMapper = getCacheJacksonMapper();
        } else {
            objectMapper = mapper;
        }
        return objectMapper.readValue(json, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T toBean(final String json, final TypeReference valueTypeRef) throws IOException {
        return (T) toBean(json, valueTypeRef, false);
    }

    public static <T> T toBean(final String json, final TypeReference valueTypeRef, final boolean isCache) throws IOException {
        ObjectMapper objectMapper;
        if (isCache) {
            objectMapper = getCacheJacksonMapper();
        } else {
            objectMapper = mapper;
        }
        return objectMapper.readValue(json, valueTypeRef);
    }

    private static ObjectMapper getCacheJacksonMapper() {
//        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
//        return (ObjectMapper) context.getBean("jacksonObjectMapper");
        
        return mapper;
    }
}
