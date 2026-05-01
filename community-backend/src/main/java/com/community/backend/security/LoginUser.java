package com.community.backend.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录态用户对象：写入 SecurityContext 的 Principal 实体。
 */
@Getter
public class LoginUser implements UserDetails {

    /**
     * 当前登录业务用户 ID，供控制器和服务层做数据隔离。
     */
    private final Long userId;
    /**
     * Spring Security principal 中保存的用户名。
     */
    private final String username;
    /**
     * UserDetails 接口要求的密码字段，这里不参与真实密码校验。
     */
    private final String password;
    /**
     * 当前账号是否启用。
     */
    private final boolean enabled;
    /**
     * Spring Security 识别的权限集合。
     */
    private final List<SimpleGrantedAuthority> authorities;

    /**
     * 基于业务用户信息构建 Spring Security 所需字段。
     */
    public LoginUser(Long userId, String username, String password, boolean enabled, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    /**
     * 返回角色列表（如 ROLE_USER）。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
