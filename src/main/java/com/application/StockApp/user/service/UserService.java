package com.application.StockApp.user.service;

import com.application.StockApp.user.model.User;
import com.application.StockApp.user.repository.UserRepository;
import com.application.StockApp.web.dto.UserRegisterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(UserRegisterDto userRegisterDto) {
        User user = User.builder()
                .username(userRegisterDto.username())
                .fullName(userRegisterDto.fullName())
                .email(userRegisterDto.email())
                .password(passwordEncoder.encode(userRegisterDto.password()))
                .role("USER")
                .build();

        return userRepository.save(user);
    }

    public User getUser(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
    }

}
