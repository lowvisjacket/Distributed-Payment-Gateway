package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Data.Repository.BusinessRepository;
import com.example.payment_processor.Utility.Enum.BusinessCategory;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessRepository businessRepository;

    public Business createBusiness(String name, BusinessCategory businessCategory) throws IllegalActionException {
        if (name == null || name.isBlank()) {
            throw new IllegalActionException("Business name is required.");
        }
        if (businessCategory == null) {
            throw new IllegalActionException("Business category is required.");
        }

        return businessRepository.save(new Business(name.trim(), businessCategory));
    }

    public Business getBusinessById(UUID businessId) throws IllegalActionException {
        if (businessId == null) {
            throw new IllegalActionException("Business id is required.");
        }

        return businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalActionException("Business not found for id: " + businessId));
    }
}
