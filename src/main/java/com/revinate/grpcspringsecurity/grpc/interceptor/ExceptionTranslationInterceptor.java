package com.revinate.grpcspringsecurity.grpc.interceptor;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.ThrowableAnalyzer;

import java.util.Objects;

@GRpcGlobalInterceptor
@Order(20)
@Slf4j
public class ExceptionTranslationInterceptor implements ServerInterceptor {

    private ThrowableAnalyzer throwableAnalyzer = new ThrowableAnalyzer();
    private AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
        return new SimpleForwardingServerCallListener<ReqT>(delegate) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                    log.debug("Chain processed normally");
                } catch (Exception e) {
                    Throwable[] causeChain = throwableAnalyzer.determineCauseChain(e);
                    AuthenticationException authenticationException = (AuthenticationException) throwableAnalyzer
                            .getFirstThrowableOfType(AuthenticationException.class, causeChain);

                    if (Objects.nonNull(authenticationException)) {
                        handleAuthenticationException(authenticationException);
                    } else {
                        AccessDeniedException accessDeniedException = (AccessDeniedException) throwableAnalyzer
                                .getFirstThrowableOfType(AccessDeniedException.class, causeChain);

                        if (Objects.nonNull(accessDeniedException)) {
                            handleAccessDeniedException(accessDeniedException);
                        } else {
                            throw e;
                        }
                    }
                }
            }

            private void handleAuthenticationException(AuthenticationException exception) {
                log.debug("Authentication exception occurred, closing call with UNAUTHENTICATED", exception);
                call.close(Status.UNAUTHENTICATED.withDescription(exception.getMessage())
                        .withCause(exception), new Metadata());
            }

            private void handleAccessDeniedException(AccessDeniedException exception) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authenticationTrustResolver.isAnonymous(authentication)) {
                    log.debug("Access is denied (user is anonymous), closing call with UNAUTHENTICATED", exception);
                    call.close(Status.UNAUTHENTICATED.withDescription("Authentication is required to access this resource")
                            .withCause(exception), new Metadata());
                } else {
                    log.debug("Access is denied (user is not anonymous), closing call with PERMISSION_DENIED", exception);
                    call.close(Status.PERMISSION_DENIED.withDescription(exception.getMessage())
                            .withCause(exception), new Metadata());
                }
            }
        };
    }
}
