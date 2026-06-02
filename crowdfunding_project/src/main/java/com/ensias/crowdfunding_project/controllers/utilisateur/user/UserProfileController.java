package com.ensias.crowdfunding_project.controllers.utilisateur.user;

import com.ensias.crowdfunding_project.dto.utilisateur.user.UserProfileResponse;
import com.ensias.crowdfunding_project.dto.utilisateur.user.UserProfileUpdateRequest;
import com.ensias.crowdfunding_project.security.user.CustomUserDetails;
import com.ensias.crowdfunding_project.services.utilisateur.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(userProfileService.getProfile(user.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal CustomUserDetails user,
                                                             @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(user.getId(), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal CustomUserDetails user) {
        userProfileService.deleteAccount(user.getId());
        return ResponseEntity.noContent().build();
    }
}
