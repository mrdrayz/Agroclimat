package com.agroclimate.service;

import com.agroclimate.domain.entity.AppUser;
import com.agroclimate.domain.entity.Role;
import com.agroclimate.domain.enums.RoleCode;
import com.agroclimate.domain.enums.UserStatus;
import com.agroclimate.dto.auth.AuthResponse;
import com.agroclimate.dto.auth.CurrentUserDto;
import com.agroclimate.dto.auth.LoginRequest;
import com.agroclimate.dto.auth.RegisterRequest;
import com.agroclimate.exception.BadRequestException;
import com.agroclimate.repository.AppUserRepository;
import com.agroclimate.repository.RoleRepository;
import com.agroclimate.security.CustomUserDetails;
import com.agroclimate.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final List<RoleCode> ROLE_ORDER = List.of(
            RoleCode.ADMIN, RoleCode.AGRONOMIST, RoleCode.OPERATOR, RoleCode.VIEWER
    );

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GreenhouseAccessService greenhouseAccessService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BadRequestException("Email already registered");
        }

        // Все самостоятельно регистрирующиеся пользователи получают только роль VIEWER.
        // Повышение прав (AGRONOMIST/OPERATOR/ADMIN) выполняется администратором отдельно.
        Role viewer = roleRepository.findByCode(RoleCode.VIEWER)
                .orElseThrow(() -> new BadRequestException("Default role VIEWER is not seeded"));
        Set<Role> roles = new HashSet<>();
        roles.add(viewer);

        AppUser user = AppUser.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        user = userRepository.save(user);

        CustomUserDetails details = new CustomUserDetails(user);
        String token = jwtService.generateToken(details);

        return new AuthResponse(
                token, "Bearer",
                user.getId(), user.getEmail(), user.getFullName(),
                sortedRoleNames(user)
        );
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        AppUser user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadRequestException("User not found"));

        CustomUserDetails details = new CustomUserDetails(user);
        String token = jwtService.generateToken(details);

        return new AuthResponse(
                token, "Bearer",
                user.getId(), user.getEmail(), user.getFullName(),
                sortedRoleNames(user)
        );
    }

    public CurrentUserDto currentUser(AppUser user) {
        return new CurrentUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                sortedRoleNames(user),
                greenhouseAccessService.summaryFor(user)
        );
    }

    private List<String> sortedRoleNames(AppUser user) {
        return user.getRoles().stream()
                .map(r -> r.getCode().name())
                .distinct()
                .sorted(Comparator.comparingInt(roleName -> {
                    try {
                        int i = ROLE_ORDER.indexOf(RoleCode.valueOf(roleName));
                        return i < 0 ? Integer.MAX_VALUE : i;
                    } catch (IllegalArgumentException ex) {
                        return Integer.MAX_VALUE;
                    }
                }))
                .toList();
    }
}
