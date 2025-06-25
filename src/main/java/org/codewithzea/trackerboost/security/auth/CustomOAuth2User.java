package org.codewithzea.trackerboost.security.auth;


import org.codewithzea.trackerboost.user.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final UserEntity userEntity;

    public CustomOAuth2User(OAuth2User oAuth2User, UserEntity userEntity) {
        this.oAuth2User = oAuth2User;
        this.userEntity = userEntity;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(userEntity.getRole().name()));
    }

    @Override
    public String getName() {
        return oAuth2User.getAttribute("name");
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }
}
