package com.revinate.grpcspringsecurity.grpc;

import com.revinate.grpcspringsecurity.util.BasicAuthenticationCallCredentials;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.After;
import org.junit.Before;
import org.lognet.springboot.grpc.autoconfigure.GRpcServerProperties;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class GrpcServiceTestsBase {

    @Autowired
    private GRpcServerProperties grpcSettings;

    protected ManagedChannel channel;

    protected CallCredentials credentials;

    @Before
    public void baseSetup() {
        channel = ManagedChannelBuilder.forAddress("localhost", grpcSettings.getPort())
                .usePlaintext(true)
                .build();
        credentials = new BasicAuthenticationCallCredentials("grpcspring", "grpcspring");
    }

    @After
    public void baseTearDown() {
        channel.shutdown();
    }
}
