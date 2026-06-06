package gt.app.api;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

@Slf4j
public class InternalKeycloakAuthConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientRepository authorizedClientRepository) {

        var clientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .refreshToken()
            .build();

        var manager = new DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientRepository);
        manager.setAuthorizedClientProvider(clientProvider);

        return manager;
    }

    @Bean
    public RequestInterceptor bearerAuthRequestInterceptor(
        OAuth2AuthorizedClientManager authorizedClientManager) {
        return template -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return;
            }

            OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("oidc")
                .principal(authentication)
                .build();

            OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
            if (client != null) {
                log.debug("Propagating access token to {}", template.url());
                template.header(AUTHORIZATION_HEADER,
                    "%s %s".formatted(BEARER, client.getAccessToken().getTokenValue()));
            }
        };
    }
}
