package com.nbs.client.assassins;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.googlecode.androidannotations.annotations.rest.Accept;
import com.googlecode.androidannotations.annotations.rest.Rest;
import com.googlecode.androidannotations.annotations.rest.Post;
import com.googlecode.androidannotations.api.rest.MediaType;

@Rest(rootUrl = "http://hunted.cloudfoundry.com/services", converters = { MappingJacksonHttpMessageConverter.class })
public interface HuntedRestClient {
	
	@Post("/location")
	@Accept(MediaType.APPLICATION_JSON)
	User updateLocation(User userWithLocation);	

	@Post("/users")
	@Accept(MediaType.APPLICATION_JSON)
	User registerUser(User u);
	
	//need access to RestTemplate to subvert a bug in HttpUrlConnection
	//See: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
	public RestTemplate getRestTemplate();
}

