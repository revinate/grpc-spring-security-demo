package com.revinate.grpcspringsecurity.grpc;

import com.revinate.demo.*;
import com.revinate.grpcspringsecurity.NumberService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {"grpc.port: 15001"})
public class DemoGrpcServiceTests extends GrpcServiceTestsBase {

    @MockBean
    private NumberService numberService;

    @Test
    public void fibonacci_shouldReturnValue() throws Exception {
        when(numberService.fibonacci(3)).thenReturn(2);

        DemoServiceGrpc.DemoServiceBlockingStub blockingStub = DemoServiceGrpc.newBlockingStub(channel).withCallCredentials(credentials);
        FibonacciResponse response = blockingStub.fibonacci(FibonacciRequest.newBuilder().setValue(3).build());

        assertThat(response.getValue()).isEqualTo(2);
    }

    @Test
    public void factorial_shouldReturnValue() throws Exception {
        when(numberService.factorial(3)).thenReturn(6);

        DemoServiceGrpc.DemoServiceBlockingStub blockingStub = DemoServiceGrpc.newBlockingStub(channel).withCallCredentials(credentials);
        FactorialResponse response = blockingStub.factorial(FactorialRequest.newBuilder().setValue(3).build());

        assertThat(response.getValue()).isEqualTo(6);
    }
}
