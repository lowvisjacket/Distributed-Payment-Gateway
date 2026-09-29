package com.example.distributed_payment_gateway.REST;

import com.example.distributed_payment_gateway.Data.Business;
import com.example.distributed_payment_gateway.Data.Customer;
import com.example.distributed_payment_gateway.Data.Wallet;
import com.example.distributed_payment_gateway.Service.BusinessService;
import com.example.distributed_payment_gateway.Service.CustomerService;
import com.example.distributed_payment_gateway.Service.WebhookSubscriptionService;
import com.example.distributed_payment_gateway.Security.AuthenticatedCustomer;
import com.example.distributed_payment_gateway.Utility.Exception.IllegalActionException;
import com.example.distributed_payment_gateway.Utility.Record.BusinessRecord;
import com.example.distributed_payment_gateway.Utility.Record.WebhookRecord;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business")
@PreAuthorize("isAuthenticated()")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private WebhookSubscriptionService webhookSubscriptionService;

    @PostMapping("/webhook")
    public ResponseEntity<WebhookRecord.RegistrationResponse> registerWebhook(
            @AuthenticationPrincipal AuthenticatedCustomer userDetails,
            @RequestBody @Valid WebhookRecord.RegistrationRequest request
    ) throws IllegalActionException {
        Business business = customerService.getCustomerById(userDetails.getCustomerId()).getBusiness();
        if (business == null) {
            throw new IllegalActionException("Business not found for customer.");
        }
        var subscription = webhookSubscriptionService.register(
                business.getBusinessId(),
                request.url()
        );
        return ResponseEntity.ok(new WebhookRecord.RegistrationResponse(
                subscription.getId(),
                subscription.getBusinessId(),
                subscription.getUrl(),
                subscription.getSecret()
        ));
    }

    @GetMapping("/")
    public ResponseEntity<BusinessRecord.Response> getBusiness(@AuthenticationPrincipal AuthenticatedCustomer user) throws IllegalActionException {
        Customer customer = customerService.getCustomerById(user.getCustomerId());
        Business business = customer.getBusiness();
        if (business == null) {
            throw new IllegalActionException("Business not found for customer.");
        }
        return ResponseEntity.ok(BusinessRecord.Response.from(business));
    }

    @GetMapping("/wallet")
    public ResponseEntity<Wallet> getWallet(@AuthenticationPrincipal AuthenticatedCustomer customer) throws IllegalActionException {
        return ResponseEntity.ok(businessService.getBusinessByCustomerId(customer.getCustomerId()).getWallet());
    }

    @PostMapping("/refund")
    public ResponseEntity<Wallet> refund(
            @AuthenticationPrincipal AuthenticatedCustomer customer,
            @RequestBody @Valid BusinessRecord.RefundFundsRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
            ) throws IllegalActionException {
        return ResponseEntity.ok(
                businessService.refund(customer.getCustomerId(),
                        request.recipientCustomerId(),
                        request.recipientBusinessId(),
                        request.amount(), idempotencyKey
                )
        );
    }
}
