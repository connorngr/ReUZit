package com.connorng.ReUzit.controller.auth;

import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private AuthenticationService authenticationService;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${secret-password}")
    private String defaultPassword;

    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AuthenticationResponse> register(
            @RequestParam("user") String userJson,
            @RequestParam("imageUrl") MultipartFile file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        RegisterRequest registerRequest = objectMapper.readValue(userJson, RegisterRequest.class);

        AuthenticationResponse response = authenticationService.register(registerRequest, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    };

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponse> grantCode(@RequestParam("code") String code) throws Exception {
        String userInfo = processGrantCode(code);

        User googleUser = extractUserInfo(userInfo);
        googleUser.setPassword(defaultPassword);

        return ResponseEntity.ok(authenticationService.googleAuth(googleUser));
    }

    private String processGrantCode(String code) throws JsonProcessingException, JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", "http://localhost:5173/google/callback");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("grant_type", "authorization_code");

        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, httpHeaders);

        String tokenUrl = "https://oauth2.googleapis.com/token";

        // Send POST request to exchange authorization code for an access token
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

        // Extract the access token from the response (JSON format)
        String responseBody = response.getBody();

        // You can use a library like Jackson to parse the JSON and extract the token
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        String accessToken = rootNode.path("access_token").asText();

        // Use the access token to get user info
        return getUserInfo(accessToken);
    };
    private String getUserInfo(String accessToken) {
        // Prepare the request to get user info
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
        RestTemplate restTemplate = new RestTemplate();

        // Send GET request to fetch user info
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, null, String.class);

        // Process the user info from the response (you can store or return it)
        String userInfo = response.getBody();

        // Example: Returning user info as a JSON string
        return userInfo;
    }

    private User extractUserInfo(String userInfo) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the JSON string returned by Google
        JsonNode userJson = objectMapper.readTree(userInfo);

        // Create a new User object and populate its fields
        User user = new User();
        user.setEmail(userJson.get("email").asText());
        user.setFirstName(userJson.get("family_name").asText());
        user.setLastName(userJson.get("given_name").asText());
        user.setImageUrl(userJson.get("picture").asText());

        // Optionally set default or random password if needed (for first-time registration)
        user.setPassword("defaultPassword"); // Or generate a random password

        return user;
    }
}
