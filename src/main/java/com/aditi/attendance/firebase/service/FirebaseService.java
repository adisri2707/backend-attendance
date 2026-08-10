package com.aditi.attendance.firebase.service;

import com.aditi.attendance.firebase.dto.FirebaseUser;
import com.aditi.attendance.user.exception.UserNotFoundException;
import com.aditi.attendance.user.repository.UserRepository;
import com.aditi.attendance.entity.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseService {

    private final UserRepository userRepository;

    public FirebaseUser verifyToken(String idToken) {

        try {

            FirebaseToken firebaseToken = FirebaseAuth.getInstance()
                    .verifyIdToken(idToken);

            User user = userRepository.findByEmployeeEmail(firebaseToken.getEmail())
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "User with email " + firebaseToken.getEmail() + " not found."
                            )
                    );

            return FirebaseUser.builder()
                    .uid(firebaseToken.getUid())
                    .email(firebaseToken.getEmail())
                    .name(firebaseToken.getName())
                    .emailVerified(firebaseToken.isEmailVerified())
                    .role(user.getRole().getRoleName())
                    .build();

        } catch (FirebaseAuthException exception) {

            throw new RuntimeException(
                    "Invalid Firebase ID Token.",
                    exception
            );
        }
    }
}