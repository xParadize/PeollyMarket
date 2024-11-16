package com.peolly.securityserver.usermicroservice.services;

import com.peolly.securityserver.usermicroservice.enums.Role;
import com.peolly.securityserver.usermicroservice.model.User;
import com.peolly.securityserver.usermicroservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository usersRepository;

    // TODO: rename method
    @Transactional
    public void save(User userToSave) {
        usersRepository.save(userToSave);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findById(UUID userid) {
        return usersRepository.findById(userid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Получение пользователя по имени пользователя
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    @Transactional(readOnly = true)
    public UserDetailsService userDetailsService() {
        return this::findByUsername;
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }

    /**
     * Выдача прав администратора текущему пользователю
     * Нужен для демонстрации
     */
    @Transactional
    @Deprecated
    public void getAdmin() {
        var user = getCurrentUser();
        user.getRoles().add(Role.ROLE_ADMIN);
        save(user);
    }

    @Transactional
    @Deprecated
    public void getCompanyManager() {
        var user = getCurrentUser();
        user.getRoles().add(Role.ROLE_COMPANY_MANAGER);
        save(user);
    }

    @Transactional
    public void getVerificatedRole(UUID userId) {
        usersRepository.getVerificatedRole(userId);
    }
}
