package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Repository.BusinessRepository;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Utility.Enum.BusinessCategory;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessRepository businessRepository;
    private final WalletService walletService;

    public Business createBusiness(String name, BusinessCategory businessCategory, Customer customer) throws IllegalActionException {
        if (name == null || name.isBlank()) {
            throw new IllegalActionException("Business name is required.");
        }
        if (businessCategory == null) {
            throw new IllegalActionException("Business category is required.");
        }
        Business business = new Business(name.trim(), businessCategory, customer);
        businessRepository.save(business);
        Wallet wallet = walletService.createWalletViaBusiness(business, null);
        business.setWallet(wallet);
        return businessRepository.save(business);
    }

    public Business getBusinessById(UUID businessId) throws IllegalActionException {
        if (businessId == null) {
            throw new IllegalActionException("Business id is required.");
        }

        return businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalActionException("Business not found for id: " + businessId));
    }
}
