package gt.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.http.client.ClientHttpRequestInterceptor;

@Configuration
@Slf4j
public class InternalKeycloakAuthConfig {

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
    public ClientHttpRequestInterceptor bearerAuthInterceptor(
        OAuth2AuthorizedClientManager authorizedClientManager) {
        return (request, body, execution) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return execution.execute(request, body);
            }

            OAuth2AuthorizeRequest oauthRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("oidc")
                .principal(authentication)
                .build();

            OAuth2AuthorizedClient client = authorizedClientManager.authorize(oauthRequest);
            if (client != null) {
                log.debug("Propagating access token to {}", request.getURI());
                request.getHeaders().setBearerAuth(client.getAccessToken().getTokenValue());
            }
            return execution.execute(request, body);
        };
    }
}
