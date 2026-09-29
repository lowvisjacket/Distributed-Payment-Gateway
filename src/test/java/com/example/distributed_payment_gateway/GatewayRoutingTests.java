package com.example.distributed_payment_gateway;

import com.example.distributed_payment_gateway.Security.Jwt.JwtService;
import com.example.distributed_payment_gateway.Service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cloud.gateway.server.mvc.GatewayServerMvcAutoConfiguration;
import org.springframework.cloud.gateway.server.mvc.filter.FilterAutoConfiguration;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctionAutoConfiguration;
import org.springframework.cloud.gateway.server.mvc.predicate.PredicateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GatewayRoutingTests.ApiProbeController.class,
        properties = {
                "spring.cloud.gateway.server.webmvc.routes[0].id=local-api",
                "spring.cloud.gateway.server.webmvc.routes[0].uri=forward:/api{path}",
                "spring.cloud.gateway.server.webmvc.routes[0].predicates[0]=Path=/gateway/api/{*path}"
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration({
        GatewayServerMvcAutoConfiguration.class,
        FilterAutoConfiguration.class,
        HandlerFunctionAutoConfiguration.class,
        PredicateAutoConfiguration.class
})
@Import(GatewayRoutingTests.MockCustomerConfiguration.class)
class GatewayRoutingTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void gatewayForwardsRequestToLocalApiPath() throws Exception {
        mockMvc.perform(get("/gateway/api/ping"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/api/ping"));
    }

    @RestController
    static class ApiProbeController {
        @GetMapping("/api/ping")
        String ping() {
            return "pong";
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class MockCustomerConfiguration {
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        CustomerService customerService() {
            return mock(CustomerService.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }
    }
}
