package com.example.payment_processor.Utility.Record;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Utility.Enum.BusinessCategory;

import java.util.UUID;

public class BusinessRecord {
    public record Response(
            UUID businessId,
            String name,
            BusinessCategory businessCategory,
            UUID customerId,
            UUID walletId
    ) {
        public static Response from(Business business) {
            return new Response(
                    business.getBusinessId(),
                    business.getName(),
                    business.getBusinessCategory(),
                    business.getCustomer() == null ? null : business.getCustomer().getId(),
                    business.getWallet() == null ? null : business.getWallet().getId()
            );
        }
    }
}
