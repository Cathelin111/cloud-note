package com.lcz.cloud_note.web;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON消息转换器
 * 说明：部署版提供的spring-webmvc-3.2.8.RELEASE.jar已损坏，
 * 其中的org.springframework.http.converter.json包(Jackson转换器)整体缺失，
 * 因此自定义一个基于jackson-databind的转换器，并在spring_mvc.xml中通过
 * &lt;mvc:message-converters&gt;注册(register-defaults默认false,不会加载缺失的默认转换器)。
 */
public class JsonMessageConverter implements HttpMessageConverter<Object> {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private ObjectMapper objectMapper = new ObjectMapper();
	private List<MediaType> supportedMediaTypes = Collections.singletonList(
			new MediaType("application", "json", UTF8));
	
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return true;
	}
	
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return true;
	}
	
	public List<MediaType> getSupportedMediaTypes() {
		return supportedMediaTypes;
	}
	
	public Object read(Class<? extends Object> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		return objectMapper.readValue(inputMessage.getBody(), clazz);
	}
	
	public void write(Object o, MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		outputMessage.getHeaders().setContentType(new MediaType("application", "json", UTF8));
		objectMapper.writeValue(outputMessage.getBody(), o);
	}
}
