package com.revinate.grpcspringsecurity.grpc.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;

@GRpcGlobalInterceptor
@Order(50)
@Slf4j
public class BasicAuthenticationInterceptor implements ServerInterceptor {

    private final AuthenticationManager authenticationManager;

    @Autowired
    public BasicAuthenticationInterceptor(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        String authHeader = nullToEmpty(headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)));
        if (!authHeader.startsWith("Basic ")) {
            return next.startCall(call, headers);
        }

        try {
            String[] tokens = decodeBasicAuth(authHeader);
            String username = tokens[0];

            log.debug("Basic Authentication Authorization header found for user: {}", username);

            if (authenticationIsRequired(username)) {
                Authentication authRequest = new UsernamePasswordAuthenticationToken(username, tokens[1]);
                Authentication authResult = authenticationManager.authenticate(authRequest);

                log.debug("Authentication success: {}", authResult);

                SecurityContextHolder.getContext().setAuthentication(authResult);
            }
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();

            log.debug("Authentication request failed: {}", e.getMessage());

            throw Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e).asRuntimeException();
        }

        return next.startCall(call, headers);
    }

    private String[] decodeBasicAuth(String authHeader) {
        String basicAuth;
        try {
            basicAuth = new String(Base64.getDecoder().decode(authHeader.substring(6).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }

        int delim = basicAuth.indexOf(":");
        if (delim == -1) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }

        return new String[] { basicAuth.substring(0, delim), basicAuth.substring(delim + 1) };
    }

    private boolean authenticationIsRequired(String username) {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(existingAuth) || !existingAuth.isAuthenticated()) {
            return true;
        }

        if (existingAuth instanceof UsernamePasswordAuthenticationToken
                && !existingAuth.getName().equals(username)) {
            return true;
        }

        if (existingAuth instanceof AnonymousAuthenticationToken) {
            return true;
        }

        return false;
    }
}
