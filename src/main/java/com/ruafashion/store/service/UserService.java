package com.ruafashion.store.service;


import com.ruafashion.store.dto.RegisterRequest;
import com.ruafashion.store.model.Role;
import com.ruafashion.store.model.User;
import com.ruafashion.store.repository.RoleRepository;
import com.ruafashion.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
            .orElseThrow(() -> new RuntimeException("Không tìm thấy role CUSTOMER"));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setStatus(true);
        user.setRole(customerRole);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }
}