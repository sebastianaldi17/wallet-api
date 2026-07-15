package com.sebastianaldi17.walletapi.components;

import com.sebastianaldi17.walletapi.models.UserApiKey;
import com.sebastianaldi17.walletapi.repositories.UserApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    private static final String API_KEY_HEADER = "X-API-KEY";

    private final UserApiKeyRepository userApiKeyRepository;

    public ApiKeyFilter(UserApiKeyRepository userApiKeyRepository) {
        this.userApiKeyRepository = userApiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey == null) {
            setUnauthorizedResponse(response);
            return;
        }

        Optional<UserApiKey> userApiKeyOptional =  userApiKeyRepository.findOneByApiKey(requestApiKey);
        if(userApiKeyOptional.isEmpty()) {
            setUnauthorizedResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Unauthorized: Invalid or missing API Key\"}");
    }
}
