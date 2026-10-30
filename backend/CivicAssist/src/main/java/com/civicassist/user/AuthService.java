package com.civicassist.user;

import com.civicassist.security.JwtService;
import com.civicassist.user.dto.AuthResponse;
import com.civicassist.user.dto.LoginRequest;
import com.civicassist.user.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    )
    {
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.authenticationManager=authenticationManager;
        this.jwtService=jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request){
        String email=request.email().trim().toLowerCase();
        if(userRepository.existsByEmail(email)){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,"Email is already registered"
                    );
        }
        AppUser user=new AppUser(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password())
        );
        user=userRepository.saveAndFlush(user);
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest request){
        String email=request.email().trim().toLowerCase(Locale.ROOT);
        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email,request.password())
        );

        AppUser user=(AppUser) authentication.getPrincipal();
        return buildResponse(user);
    }

    private AuthResponse buildResponse(AppUser user){
        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
