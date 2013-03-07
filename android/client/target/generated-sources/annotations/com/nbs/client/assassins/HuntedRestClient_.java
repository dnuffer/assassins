//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations.
//


package com.nbs.client.assassins;

import java.util.Collections;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class HuntedRestClient_
    implements HuntedRestClient
{

    private RestTemplate restTemplate;
    private String rootUrl;

    public HuntedRestClient_() {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
        rootUrl = "http://hunted.cloudfoundry.com/services";
    }

    @Override
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    @Override
    public User updateLocation(User userWithLocation) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<User> requestEntity = new HttpEntity<User>(userWithLocation, httpHeaders);
        return restTemplate.exchange(rootUrl.concat("/location"), HttpMethod.POST, requestEntity, User.class).getBody();
    }

    @Override
    public User registerUser(User u) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<User> requestEntity = new HttpEntity<User>(u, httpHeaders);
        return restTemplate.exchange(rootUrl.concat("/users"), HttpMethod.POST, requestEntity, User.class).getBody();
    }

}
