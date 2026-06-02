package com.ensias.crowdfunding_project.controllers.utilisateur.user;

import com.ensias.crowdfunding_project.dto.utilisateur.user.UserDashboardResponse;
import com.ensias.crowdfunding_project.security.user.CustomUserDetails;
import com.ensias.crowdfunding_project.services.utilisateur.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserDashboardController {

    private final UserDashboardService userDashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<UserDashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userDashboardService.getDashboard(user.getId()));
    }
}