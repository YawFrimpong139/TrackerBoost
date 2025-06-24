package org.codewithzea.trackerboost.user;

import lombok.RequiredArgsConstructor;
import org.codewithzea.trackerboost.user.dto.UserDTO;
import org.codewithzea.trackerboost.user.dto.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserDTO registerUser(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save
        UserEntity savedUser = userRepository.save(user);

        // Map to DTO
        return userMapper.toDTO(savedUser);
    }

}