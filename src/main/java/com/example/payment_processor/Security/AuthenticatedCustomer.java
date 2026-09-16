package com.example.payment_processor.Security;

import com.example.payment_processor.Security.Email.VerificationCodeService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class AuthenticatedCustomer implements UserDetails {
    private final UUID customerId;
    private final UserDetails delegate;
    private final VerificationCodeService verificationCodeService;

    public AuthenticatedCustomer(UUID customerId, UserDetails delegate) {
        this(customerId, delegate, null);
    }

    public AuthenticatedCustomer(UUID customerId, UserDetails delegate, VerificationCodeService verificationCodeService) {
        this.customerId = customerId;
        this.delegate = delegate;
        this.verificationCodeService = verificationCodeService;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getPassword() {
        return delegate.getPassword();
    }

    @Override
    public String getUsername() {
        return delegate.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return delegate.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return delegate.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return delegate.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }
}
