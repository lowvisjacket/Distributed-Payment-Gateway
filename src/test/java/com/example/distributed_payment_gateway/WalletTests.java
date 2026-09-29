package com.example.distributed_payment_gateway;

import com.example.distributed_payment_gateway.Service.CustomerService;
import com.example.distributed_payment_gateway.Service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
public class WalletTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WalletService walletService;

    @Autowired
    CustomerService customerService;
}
