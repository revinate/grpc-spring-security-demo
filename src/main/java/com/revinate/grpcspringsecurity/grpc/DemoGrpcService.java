package com.revinate.grpcspringsecurity.grpc;

import com.revinate.demo.*;
import com.revinate.grpcspringsecurity.NumberService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@GRpcService
public class DemoGrpcService extends DemoServiceGrpc.DemoServiceImplBase {

    private final NumberService numberService;

    @Autowired
    public DemoGrpcService(NumberService numberService) {
        this.numberService = numberService;
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public void fibonacci(FibonacciRequest request, StreamObserver<FibonacciResponse> responseObserver) {
        if (request.getValue() < 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Number cannot be negative").asRuntimeException());
            return;
        }

        FibonacciResponse response = FibonacciResponse.newBuilder()
                .setValue(numberService.fibonacci(request.getValue()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @PreAuthorize("hasRole('VIEWER')")
    public void factorial(FactorialRequest request, StreamObserver<FactorialResponse> responseObserver) {
        if (request.getValue() < 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Number cannot be negative").asRuntimeException());
            return;
        }

        FactorialResponse response = FactorialResponse.newBuilder()
                .setValue(numberService.factorial(request.getValue()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
