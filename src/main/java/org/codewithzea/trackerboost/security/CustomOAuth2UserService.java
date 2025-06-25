package org.codewithzea.trackerboost.security;


import org.codewithzea.trackerboost.security.auth.CustomOAuth2User;
import org.codewithzea.trackerboost.user.UserEntity;
import org.codewithzea.trackerboost.user.UserRepository;
import org.codewithzea.trackerboost.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        //String domain = email.substring(email.indexOf("@") + 1);

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found in OAuth2 user");
        }

        // First find or create the UserEntity
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    String[] names = name != null ? name.split(" ") : new String[]{"User", ""};

                    return userRepository.save(UserEntity.builder()
                            .firstName(names[0])
                            .lastName(names.length > 1 ? names[1] : "")
                            .email(email)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .role(Role.ROLE_CONTRACTOR) // Default role
                            .build());
                });


        return new CustomOAuth2User(oAuth2User, userEntity);
    }
}