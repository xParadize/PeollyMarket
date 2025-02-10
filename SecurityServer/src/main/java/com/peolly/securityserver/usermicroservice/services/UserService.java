package com.peolly.securityserver.usermicroservice.services;

import com.peolly.securityserver.usermicroservice.enums.UserRole;
import com.peolly.securityserver.exceptions.IncorrectRoleInput;
import com.peolly.securityserver.exceptions.MissingRoleException;
import com.peolly.securityserver.exceptions.RepeatedRoleException;
import com.peolly.securityserver.usermicroservice.model.User;
import com.peolly.securityserver.usermicroservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository usersRepository;

    @Transactional
    public void saveUser(User userToSave) {
        usersRepository.save(userToSave);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findById(UUID userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public String findEmailByUserId(UUID userId) {
        return usersRepository.getEmailByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public String findEmailByUsername(String username) {
        return usersRepository.getEmailByUsername(username)
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

    @Transactional
    @Deprecated
    public void updateUserRole(String requestedRole, boolean isAdding) {
        var user = getCurrentUser();
        var userRole = convertStringToUserRole(requestedRole);

        if (!doesUserRoleExist(requestedRole)) {
            throw new IncorrectRoleInput();
        }

        if (isAdding) {
            if (user.getRoles().contains(userRole)) {
                throw new RepeatedRoleException();
            }
            user.getRoles().add(userRole);
        } else {
            if (!user.getRoles().contains(userRole)) {
                throw new MissingRoleException();
            }
            user.getRoles().remove(userRole);
        }
        saveUser(user);
    }

    private UserRole convertStringToUserRole(String inputString) {
        try {
            return Enum.valueOf(UserRole.class, "ROLE_" + inputString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IncorrectRoleInput();
        }
    }

    private boolean doesUserRoleExist(String inputRole) {
        return Arrays.stream(UserRole.values())
                .anyMatch(role -> role.name().equals("ROLE_" + inputRole.toUpperCase()));
    }
}
