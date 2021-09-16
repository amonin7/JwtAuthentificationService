package com.onlym.controller;

import com.onlym.config.jwt.JwtUtils;
import com.onlym.model.User;
import com.onlym.model.request.LoginRequest;
import com.onlym.model.request.RegistrationRequest;
import com.onlym.model.response.JwtResponse;
import com.onlym.model.response.MessageResponse;
import com.onlym.repo.UserRepository;
import com.onlym.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
public class MainController {

    @Autowired
    public AuthenticationManager authenticationManager;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public PasswordEncoder passwordEncoder;
    @Autowired
    public JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody RegistrationRequest registrationRequest) {
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Error: user with such username already exists")
            );
        }
        User user = new User(
                registrationRequest.getUsername(),
                passwordEncoder.encode(registrationRequest.getPassword())
        );
        userRepository.save(user);
        return ResponseEntity.ok(
                new MessageResponse("User with username: '" + user.getUsername() + "' has been created")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> authUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getUsername()
        ));
    }

    @GetMapping("/hello")
    public ResponseEntity<?> sayHello() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return ResponseEntity.ok(
                new MessageResponse("Hello, " + userDetails.getUsername() + "!")
        );
    }
}
