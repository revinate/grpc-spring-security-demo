package com.revinate.grpcspringsecurity.util;

import io.grpc.Attributes;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executor;

public final class BasicAuthenticationCallCredentials implements CallCredentials {

    private final String username;
    private final String password;

    public BasicAuthenticationCallCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void applyRequestMetadata(MethodDescriptor<?, ?> method, Attributes attrs, Executor appExecutor, MetadataApplier applier) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), encodeBasicAuth(username, password));
        applier.apply(metadata);
    }

    @Override
    public void thisUsesUnstableApi() {
        // does nothing
    }

    private String encodeBasicAuth(String username, String password) {
        return "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }
}
