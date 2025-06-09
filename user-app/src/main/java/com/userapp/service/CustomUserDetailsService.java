package com.userapp.service;

import com.userapp.entity.User;
import com.userapp.exception.DeactivatedUserException;
import com.userapp.repository.IUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.userapp.constants.ConstantStrings.USER_ACCOUNT_DEACTIVATED;
import static com.userapp.constants.ConstantStrings.USER_NOT_FOUND;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;

    public CustomUserDetailsService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new DeactivatedUserException(USER_ACCOUNT_DEACTIVATED);
        }

        return user;
    }
}
