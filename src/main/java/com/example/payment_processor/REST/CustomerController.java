package com.example.payment_processor.REST;

import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Repository.CustomerRepository;
import com.example.payment_processor.Service.CustomerService;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.example.payment_processor.Utility.Record.CustomerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    @Autowired
    CustomerService customerService;

    @GetMapping("/")
    public ResponseEntity<CustomerRecord.Response> getCustomerInfo(@AuthenticationPrincipal UserDetails customer) throws IllegalActionException {
        return ResponseEntity.ok(CustomerRecord.Response.from(
                customerService.getCustomerByEmail(customer.getUsername())
        ));
    }
}
