package com.example.payment_processor;

import com.example.payment_processor.Service.CustomerService;
import com.example.payment_processor.Service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class WalletTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WalletService walletService;

    @Autowired
    CustomerService customerService;
}
