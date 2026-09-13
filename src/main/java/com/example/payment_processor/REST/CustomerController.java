package com.example.payment_processor.REST;

import com.example.payment_processor.Data.Payment;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Service.CustomerService;
import com.example.payment_processor.Security.AuthenticatedCustomer;
import com.example.payment_processor.Service.PaymentService;
import com.example.payment_processor.Service.WalletService;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.example.payment_processor.Utility.Record.CustomerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@PreAuthorize("isAuthenticated()")
public class CustomerController {
    @Autowired
    CustomerService customerService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    WalletService walletService;

    @GetMapping("/")
    public ResponseEntity<CustomerRecord.Response> getCustomerInfo(@AuthenticationPrincipal AuthenticatedCustomer customer) throws IllegalActionException {
        return ResponseEntity.ok(CustomerRecord.Response.from(
                customerService.getCustomerById(customer.getCustomerId())
        ));
    }

    @PutMapping("/pay")
    public ResponseEntity<Payment> payBusiness(
            @AuthenticationPrincipal AuthenticatedCustomer customer,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Payment payment
    ) throws IllegalActionException {
        return ResponseEntity.ok(paymentService.createPayment(
                customer.getCustomerId(),
                payment.getBusinessId(),
                payment.getAmount(),
                idempotencyKey
        ));
    }

    @GetMapping("/wallet")
    public ResponseEntity<Wallet> getWallet(@AuthenticationPrincipal AuthenticatedCustomer customer) throws IllegalActionException {
        return ResponseEntity.ok(walletService.getWalletByCustomerId(customer.getCustomerId()));
    }
}
