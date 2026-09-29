package com.example.distributed_payment_gateway.REST;

import com.example.distributed_payment_gateway.Data.Payment;
import com.example.distributed_payment_gateway.Data.Wallet;
import com.example.distributed_payment_gateway.Service.CustomerService;
import com.example.distributed_payment_gateway.Security.AuthenticatedCustomer;
import com.example.distributed_payment_gateway.Service.PaymentService;
import com.example.distributed_payment_gateway.Service.WalletService;
import com.example.distributed_payment_gateway.Utility.Exception.IllegalActionException;
import com.example.distributed_payment_gateway.Utility.Record.CustomerRecord;
import com.example.distributed_payment_gateway.Utility.Record.PaymentRecord;
import jakarta.validation.Valid;
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
            @RequestBody @Valid PaymentRecord.CreateRequest payment
    ) throws IllegalActionException {
        return ResponseEntity.ok(paymentService.createPayment(
                customer.getCustomerId(),
                payment.businessId(),
                payment.amount(),
                idempotencyKey
        ));
    }

    @GetMapping("/wallet")
    public ResponseEntity<Wallet> getWallet(@AuthenticationPrincipal AuthenticatedCustomer customer) throws IllegalActionException {
        return ResponseEntity.ok(walletService.getWalletByCustomerId(customer.getCustomerId()));
    }
}
