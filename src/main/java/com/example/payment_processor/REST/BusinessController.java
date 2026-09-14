package com.example.payment_processor.REST;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Service.BusinessService;
import com.example.payment_processor.Service.CustomerService;
import com.example.payment_processor.Service.WebhookSubscriptionService;
import com.example.payment_processor.Security.AuthenticatedCustomer;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.example.payment_processor.Utility.Record.BusinessRecord;
import com.example.payment_processor.Utility.Record.WebhookRecord;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<BusinessRecord.Response> getBusiness(@AuthenticationPrincipal AuthenticatedCustomer userDetails) throws IllegalActionException {
        Customer customer = customerService.getCustomerById(userDetails.getCustomerId());
        Business business = customer.getBusiness();
        if (business == null) {
            throw new IllegalActionException("Business not found for customer.");
        }
        return ResponseEntity.ok(BusinessRecord.Response.from(business));
    }

    @GetMapping("/wallet")
    public ResponseEntity<Wallet> getWallet(@AuthenticationPrincipal AuthenticatedCustomer userDetails) throws IllegalActionException {
        return ResponseEntity.ok(businessService.getBusinessByCustomerId(userDetails.getCustomerId()).getWallet());
    }
}
