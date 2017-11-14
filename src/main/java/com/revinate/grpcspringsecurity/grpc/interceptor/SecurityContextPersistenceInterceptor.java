package com.revinate.grpcspringsecurity.grpc.interceptor;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;

@GRpcGlobalInterceptor
@Order(10)
@Slf4j
public class SecurityContextPersistenceInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
        return new SimpleForwardingServerCallListener<ReqT>(delegate) {
            @Override
            public void onComplete() {
                try {
                    super.onComplete();
                } finally {
                    SecurityContextHolder.clearContext();
                    log.debug("SecurityContextHolder now cleared, as request processing completed");
                }
            }

            @Override
            public void onCancel() {
                try {
                    super.onCancel();
                } finally {
                    SecurityContextHolder.clearContext();
                    log.debug("SecurityContextHolder now cleared, as request processing was canceled");
                }
            }
        };
    }
}
