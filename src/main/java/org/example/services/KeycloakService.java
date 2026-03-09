package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakService {
    private final RestTemplate rest = new RestTemplate();

    @Value("${keycloak.server-url}")
    private String serverUrl;
    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public LoginResponse login(String user, String pass) {
        // 1. Build the correct URL dynamically
        String url = UriComponentsBuilder.fromHttpUrl(serverUrl)
                .path("/realms/{realm}/protocol/openid-connect/token")
                .buildAndExpand(realm)
                .toUriString();

        // 2. Create the body (Must be MultiValueMap for form-encoding)
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", user);
        map.add("password", pass);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        // 3. Set Headers (Must be application/x-www-form-urlencoded)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            // 4. Execute the POST request
            return rest.postForObject(url, request, LoginResponse.class);
        } catch (HttpClientErrorException e) {
            // Detailed logging to see exactly why Keycloak rejected it
            System.err.println("Keycloak Error: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            throw e;
        }
    }
//    public void register(String user, String email, String pass) {
//        String adminToken = getAdminToken(); // Get token from /master realm
//        String url = serverUrl + "/admin/realms/" + realm + "/users";
//
//        var body = Map.of(
//                "username", user,
//                "email", email,
//                "enabled", true,
//                "credentials", List.of(Map.of("type", "password", "value", pass, "temporary", false))
//        );
//
//        rest.postForEntity(url, new HttpEntity<>(body, createHeaders(true, adminToken)), String.class);
//    }

    private HttpHeaders createHeaders(boolean isJson) {
        HttpHeaders headers = new HttpHeaders();
        if (isJson) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        } else {
            // Keycloak token endpoint REQUIRES this specific type
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }
        return headers;
    }
}