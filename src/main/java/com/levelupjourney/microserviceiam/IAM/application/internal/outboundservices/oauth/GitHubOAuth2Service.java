package com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class GitHubOAuth2Service {

    private final RestTemplate restTemplate;
    private final SecureRandom secureRandom;

    @Value("${GITHUB_CLIENT_ID}")
    private String clientId;

    @Value("${GITHUB_CLIENT_SECRET}")
    private String clientSecret;

    private static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USERINFO_URL = "https://api.github.com/user";
    private static final String GITHUB_EMAILS_URL = "https://api.github.com/user/emails";
    private static final String REDIRECT_URI = "http://localhost:8080/oauth2/callback";
    private static final String SCOPE = "user:email";

    public GitHubOAuth2Service() {
        this.restTemplate = new RestTemplate();
        this.secureRandom = new SecureRandom();
    }

    public String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String buildAuthorizationUrl(String state) {
        String encodedRedirectUri = URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);
        
        return GITHUB_AUTH_URL + 
               "?client_id=" + clientId +
               "&redirect_uri=" + encodedRedirectUri +
               "&response_type=code" +
               "&scope=" + encodedScope +
               "&state=" + state +
               "&allow_signup=true";
    }

    public GitHubTokenResponse exchangeCodeForTokens(String code, String state) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", REDIRECT_URI);
        body.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<GitHubTokenResponse> response = restTemplate.exchange(
                GITHUB_TOKEN_URL,
                HttpMethod.POST,
                request,
                GitHubTokenResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange code for tokens: " + e.getMessage(), e);
        }
    }

    public GitHubUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitHubUserInfo> response = restTemplate.exchange(
                GITHUB_USERINFO_URL,
                HttpMethod.GET,
                request,
                GitHubUserInfo.class
            );

            GitHubUserInfo userInfo = response.getBody();
            
            // Get user emails if email is null
            if (userInfo != null && userInfo.getEmail() == null) {
                GitHubEmail[] emails = getUserEmails(accessToken);
                if (emails != null && emails.length > 0) {
                    // Find primary email or use first verified email
                    for (GitHubEmail email : emails) {
                        if (email.isPrimary() && email.isVerified()) {
                            userInfo.setEmail(email.getEmail());
                            break;
                        }
                    }
                    // If no primary email found, use first verified email
                    if (userInfo.getEmail() == null) {
                        for (GitHubEmail email : emails) {
                            if (email.isVerified()) {
                                userInfo.setEmail(email.getEmail());
                                break;
                            }
                        }
                    }
                }
            }

            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user info: " + e.getMessage(), e);
        }
    }

    private GitHubEmail[] getUserEmails(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitHubEmail[]> response = restTemplate.exchange(
                GITHUB_EMAILS_URL,
                HttpMethod.GET,
                request,
                GitHubEmail[].class
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user emails: " + e.getMessage(), e);
        }
    }

    public static class GitHubTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("scope")
        private String scope;

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }

        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }

        public Integer getExpiresIn() { 
            // GitHub tokens don't expire, return a large value
            return 86400; // 24 hours
        }
    }

    public static class GitHubUserInfo {
        @JsonProperty("id")
        private String id;

        @JsonProperty("login")
        private String login;

        @JsonProperty("email")
        private String email;

        @JsonProperty("name")
        private String name;

        @JsonProperty("avatar_url")
        private String avatarUrl;

        @JsonProperty("bio")
        private String bio;

        @JsonProperty("location")
        private String location;

        @JsonProperty("company")
        private String company;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        // GitHub emails are always verified through the API
        public Boolean getVerifiedEmail() { return true; }

        // Use avatar_url as picture
        public String getPicture() { return avatarUrl; }
    }

    public static class GitHubEmail {
        @JsonProperty("email")
        private String email;

        @JsonProperty("verified")
        private boolean verified;

        @JsonProperty("primary")
        private boolean primary;

        @JsonProperty("visibility")
        private String visibility;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }

        public boolean isPrimary() { return primary; }
        public void setPrimary(boolean primary) { this.primary = primary; }

        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }
    }
}