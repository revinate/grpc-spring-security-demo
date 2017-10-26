package com.revinate.grpcspringsecurity.grpc.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@GRpcGlobalInterceptor
@Order(100)
@Slf4j
public class AnonymousAuthenticationInterceptor implements ServerInterceptor {

    private String key = UUID.randomUUID().toString();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        if (Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
            SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(key,
                    "anonymousUser", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

            log.debug("Populated SecurityContextHolder with anonymous token: {}",
                    SecurityContextHolder.getContext().getAuthentication());
        } else {
            log.debug("SecurityContextHolder not populated with anonymous token, as it already contained: {}",
                    SecurityContextHolder.getContext().getAuthentication());
        }

        return next.startCall(call, headers);
    }
}
