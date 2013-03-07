package com.nbs.client.assassins;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

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
}

