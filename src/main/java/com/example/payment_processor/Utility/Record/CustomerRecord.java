package com.example.payment_processor.Utility.Record;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Utility.Enum.CustomerRole;

import java.util.Currency;
import java.util.UUID;

public class CustomerRecord {
    public record Response(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            CustomerRole customerRole,
            UUID walletId
    ) {
        public static Response from(Customer customer) {
            return new Response(
                    customer.getId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail(),
                    customer.getPhoneNumber(),
                    customer.getCustomerRole(),
                    customer.getWallet().getId()
            );
        }
    }

    public record UpdateCustomer (
            String email,
            String firstName,
            String lastName
    )
    {
        public static Response from(Customer customer) {
            return new Response(
                    customer.getId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail(),
                    customer.getPhoneNumber(),
                    customer.getCustomerRole(),
                    customer.getWallet().getId()
            );
        }
    }
}
