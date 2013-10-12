package com.nbs.client.assassins.network;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.googlecode.androidannotations.annotations.rest.Accept;
import com.googlecode.androidannotations.annotations.rest.Rest;
import com.googlecode.androidannotations.annotations.rest.Post;
import com.googlecode.androidannotations.api.rest.MediaType;

@Rest(rootUrl = "https://hunted.cfapps.io/api", converters = { MappingJacksonHttpMessageConverter.class })
public interface HuntedRestClient {

	@Post("/provisional-users")
	LoginResponse registerProvisionalUser(LoginRequest loginMsg);
	
	@Post("/users")
	@Accept(MediaType.APPLICATION_JSON)
	LoginResponse registerUser(LoginRequest loginMsg);
	
	@Post("/users/{token}")
	LoginResponse registerUser(String token, LoginRequest loginMsg);
	
	@Post("/login")
	@Accept(MediaType.APPLICATION_JSON)
	LoginResponse login(LoginRequest loginMsg);
	
	@Post("/users/{token}/logout")
	@Accept(MediaType.APPLICATION_JSON)
	Response logout(String token);
	
	@Post("/users/{token}/gcm/register")
	@Accept(MediaType.APPLICATION_JSON)
	LoginResponse updateGCMRegId(String token, GCMRegistrationRequest msg);
	
	@Post("/users/{token}/gcm/unregister")
	@Accept(MediaType.APPLICATION_JSON)
	void unregisterGCMRegId(String token, GCMRegistrationRequest msg);
	
	@Post("/matches")
	@Accept(MediaType.APPLICATION_JSON)
	MatchResponse createMatch(CreateMatchRequest msg);	
	
	@Post("/matches/{matchName}/players")
	@Accept(MediaType.APPLICATION_JSON)
	MatchResponse joinMatch(String matchName, JoinMatchRequest msg);
	
	@Post("/users/{token}/location")
	@Accept(MediaType.APPLICATION_JSON)
	LocationResponse updateLocation(String token, UpdateLocationRequest msg);	
	
	@Post("/match/{matchId}/users/{token}/attack")
	@Accept(MediaType.APPLICATION_JSON)
	AttackResponse attack(String matchId, String token, UpdateLocationRequest msg);

	@Post("/matches/{matchId}/user/{userToken}/ready")
	@Accept(MediaType.APPLICATION_JSON)
	MatchResponse readyForMatch(String matchId, String userToken);
	
	//need access to RestTemplate to subvert a bug in HttpUrlConnection
	//See: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
	public RestTemplate getRestTemplate();	
}

