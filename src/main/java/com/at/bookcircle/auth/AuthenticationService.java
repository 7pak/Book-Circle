package com.at.bookcircle.auth;

import com.at.bookcircle.email.EmailService;
import com.at.bookcircle.email.EmailTemplate;
import com.at.bookcircle.role.RoleRepository;
import com.at.bookcircle.security.JwtService;
import com.at.bookcircle.user.Token;
import com.at.bookcircle.user.TokenRepository;
import com.at.bookcircle.user.User;
import com.at.bookcircle.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    @Value("${application.security.mailing.frontend.activation-url}")
    private String activationUrl;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;



    public void register(RegistrationRequest request) throws MessagingException {

        var userRole = roleRepository.findByName("USER")
                // TODO: MAKE A BETTER EXCEPTION HANDLING
                .orElseThrow(() -> new IllegalStateException("User Role Not Found"));

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);

        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);
        emailService.sendEmail(
                user.getEmail(),user.getFullName(), EmailTemplate.ACTIVATE_ACCOUNT,activationUrl,newToken,"Account Activation"
        );
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationToken();
        var token = Token.builder()
                .token(generatedToken)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepository.save(token);

        return generatedToken;
    }

    private String generateActivationToken() {
        String chars = "0123456789";
        StringBuilder generatedToken = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 6; i++) {
            generatedToken.append(chars.charAt(random.nextInt(chars.length())));
        }
        return generatedToken.toString();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request)  {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            var user = (User) auth.getPrincipal();
            var claims = new HashMap<String, Object>();
            claims.put("fullName", user.getFullName());
            var jwtToken = jwtService.generateToken(claims,user);
            return AuthenticationResponse.builder().token(jwtToken).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    public void activateAccount(String token) throws MessagingException {
        Token sentToken = tokenRepository.findByToken(token).orElseThrow(()->
                new RuntimeException("Token not found")
        );

        if (LocalDateTime.now().isAfter(sentToken.getExpiredAt())) {
            sendValidationEmail(sentToken.getUser());
            throw new RuntimeException("Activation Code Expired, new code has sent to your email");
        }
        var user  = userRepository.findById(sentToken.getUser().getId()).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        user.setEnabled(true);
        userRepository.save(user);
        sentToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(sentToken);
    }
}
