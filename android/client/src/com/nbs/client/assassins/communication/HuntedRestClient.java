package com.nbs.client.assassins.communication;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.googlecode.androidannotations.annotations.rest.Accept;
import com.googlecode.androidannotations.annotations.rest.Get;
import com.googlecode.androidannotations.annotations.rest.Rest;
import com.googlecode.androidannotations.annotations.rest.Post;
import com.googlecode.androidannotations.api.rest.MediaType;

@Rest(rootUrl = "https://hunted.cloudfoundry.com/api", converters = { MappingJacksonHttpMessageConverter.class })
public interface HuntedRestClient {

	@Post("/provisional-users")
	UserLoginResponse registerProvisionalUser(UserLoginMessage loginMsg);
		
	@Post("/users")
	@Accept(MediaType.APPLICATION_JSON)
	UserLoginResponse registerUser(UserLoginMessage loginMsg);
	
	@Post("/users/{token}")
	UserLoginResponse registerUser(String token, UserLoginMessage loginMsg);
	
	@Post("/login")
	@Accept(MediaType.APPLICATION_JSON)
	UserLoginResponse login(UserLoginMessage loginMsg);
	
	@Post("/users/{token}/gcm/register")
	@Accept(MediaType.APPLICATION_JSON)
	UserLoginResponse updateGCMRegId(String token, GCMRegistrationMessage msg);
	
	@Post("/users/{token}/gcm/unregister")
	@Accept(MediaType.APPLICATION_JSON)
	void unregisterGCMRegId(String token, GCMRegistrationMessage msg);
	
	@Post("/matches")
	@Accept(MediaType.APPLICATION_JSON)
	MatchResponse createMatch(CreateMatchMessage msg);	
	
	@Post("/matches/public/users")
	@Accept(MediaType.APPLICATION_JSON)
	MatchResponse joinPublicMatch(JoinMatchMessage msg);
	
	@Post("/matches/private/users")
	@Accept(MediaType.APPLICATION_JSON)
	MatchResponse joinPrivateMatch(JoinMatchMessage msg);
	
	@Post("/users/{token}/location")
	@Accept(MediaType.APPLICATION_JSON)
	LocationResponse updateLocation(String token, LocationMessage msg);	
	
	@Post("users/{token}/attack")
	@Accept(MediaType.APPLICATION_JSON)
	AttackResponse attack(String token, LocationMessage msg);
	
	//need access to RestTemplate to subvert a bug in HttpUrlConnection
	//See: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
	public RestTemplate getRestTemplate();

	

	
}

