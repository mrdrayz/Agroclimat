package com.agroclimate.controller;

import com.agroclimate.domain.enums.UserStatus;
import com.agroclimate.dto.UpdateUserRolesRequest;
import com.agroclimate.dto.UserDto;
import com.agroclimate.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> findAll() { return userService.findAll(); }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) { return userService.findById(id); }

    @PatchMapping("/{id}/status")
    public UserDto changeStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        return userService.changeStatus(id, status);
    }

    @PatchMapping("/{id}/roles")
    public UserDto updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest req) {
        return userService.updateRoles(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { userService.delete(id); }
}
