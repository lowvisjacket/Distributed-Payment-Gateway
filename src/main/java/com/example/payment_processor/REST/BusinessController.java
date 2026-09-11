package com.example.payment_processor.REST;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Service.BusinessService;
import com.example.payment_processor.Service.CustomerService;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.example.payment_processor.Utility.Record.BusinessRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/")
    public ResponseEntity<BusinessRecord.Response> getBusiness(@AuthenticationPrincipal UserDetails userDetails) throws IllegalActionException {
        Customer customer = customerService.getCustomerByEmail(userDetails.getUsername());
        Business business = customer.getBusiness();
        if (business == null) {
            throw new IllegalActionException("Business not found for customer.");
        }
        return ResponseEntity.ok(BusinessRecord.Response.from(business));
    }
}
