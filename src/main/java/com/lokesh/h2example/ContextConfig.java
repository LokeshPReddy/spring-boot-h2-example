package com.lokehs.h2example;	
import java.io.IOException;
	import java.nio.charset.Charset;
	import java.security.cert.CertificateException;
	import java.time.Duration;
	import java.util.Collections;
	
	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;
	import org.springframework.boot.web.servlet.FilterRegistrationBean;
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.http.HttpRequest;
	import org.springframework.http.client.BufferingClientHttpRequestFactory;
	import org.springframework.http.client.ClientHttpRequestExecution;
	import org.springframework.http.client.ClientHttpRequestInterceptor;
	import org.springframework.http.client.ClientHttpResponse;
	import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
	import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
	import org.springframework.util.StreamUtils;
	import org.springframework.web.client.RestTemplate;
	
	import com.fasterxml.jackson.annotation.JsonInclude;
	import com.fasterxml.jackson.databind.DeserializationFeature;
	
	import okhttp3.OkHttpClient;
	import okhttp3.tls.HandshakeCertificates;
	
	@Configuration
	public class ContextConfiguration {
	
		private static final Logger logger = LoggerFactory.getLogger(ContextConfiguration.class);
	
	
		@Bean(autowireCandidate = true)
		public Jackson2ObjectMapperBuilder objectMapperBuilder() {
			return  new Jackson2ObjectMapperBuilder()
					.serializationInclusion(JsonInclude.Include.NON_NULL)//
					.failOnUnknownProperties(false)
					.featuresToEnable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)//
					.indentOutput(false);
		}
	
		@Bean(autowireCandidate = true)
		public ClientHttpRequestInterceptor clientHttpRequestInterceptor() {
			return (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
				logger.info(String.format("%s %s", request.getURI(), new String(body, Charset.defaultCharset())));
				ClientHttpResponse response = execution.execute(request, body);
				logger.info(String.format("%s %s", response.getRawStatusCode(),
						StreamUtils.copyToString(response.getBody(), Charset.defaultCharset())));
				return response;
			};
		}
	
		@Bean(autowireCandidate = true)
		public OkHttpClient okHttpClient() throws IOException, CertificateException {
			HandshakeCertificates certificates = new HandshakeCertificates.Builder().addPlatformTrustedCertificates()
					.build();
			return new OkHttpClient.Builder()
					.sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager())
					.readTimeout(Duration.ofMillis(60000))
					.callTimeout(Duration.ofMillis(30000))
					.writeTimeout(Duration.ofMillis(30000))
					.connectTimeout(Duration.ofMillis(60000))
					.build();
		}
	
		@Bean(autowireCandidate = true)
		public OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory(OkHttpClient okHttpClient) {
			OkHttp3ClientHttpRequestFactory f = new OkHttp3ClientHttpRequestFactory(okHttpClient);
			f.setConnectTimeout(60000);
			f.setReadTimeout(60000);
			f.setWriteTimeout(60000);
			return f;
		}
	
		@Bean(autowireCandidate = true)
		public RestTemplate restTemplate(OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory,
				ClientHttpRequestInterceptor clientHttpRequestInterceptor) {
			RestTemplate restTemplate = new RestTemplate(
					new BufferingClientHttpRequestFactory(okHttp3ClientHttpRequestFactory));
			restTemplate.setInterceptors(Collections.singletonList(clientHttpRequestInterceptor));
			return restTemplate;
		}
	
		@Bean
		public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
			FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
			registrationBean.setFilter(new RequestResponseLoggingFilter());
			registrationBean.addUrlPatterns("*/v1");
			return registrationBean;
		}
	}
