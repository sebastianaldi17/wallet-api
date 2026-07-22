package com.sebastianaldi17.walletapi.components;

import com.sebastianaldi17.walletapi.enums.UserRole;
import com.sebastianaldi17.walletapi.models.User;
import com.sebastianaldi17.walletapi.models.UserApiKey;
import com.sebastianaldi17.walletapi.repositories.UserApiKeyRepository;
import com.sebastianaldi17.walletapi.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class AdminApiKeyFilter extends OncePerRequestFilter {
    private static final String API_KEY_HEADER = "X-API-KEY";

    private final UserApiKeyRepository userApiKeyRepository;
    private final UserRepository userRepository;

    public AdminApiKeyFilter(UserApiKeyRepository userApiKeyRepository, UserRepository userRepository) {
        this.userApiKeyRepository = userApiKeyRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/admin");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey == null) {
            setUnauthorizedResponse(response);
            return;
        }

        String hashed = ApiKeyHasher.hash(requestApiKey);

        Optional<UserApiKey> userApiKeyOptional =  userApiKeyRepository.findOneByApiKey(hashed);
        if(userApiKeyOptional.isEmpty()) {
            setUnauthorizedResponse(response);
            return;
        }

        UserApiKey userApiKey = userApiKeyOptional.get();

        Optional<User> user = userRepository.findOneById(userApiKey.getUserId());
        if(user.isEmpty() || !user.get().getRole().equals(UserRole.ADMIN)) {
            setUnauthorizedResponse(response);
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(
                userApiKey.getUserId(), null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void setUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Unauthorized: Invalid or missing API Key\"}");
    }
}
