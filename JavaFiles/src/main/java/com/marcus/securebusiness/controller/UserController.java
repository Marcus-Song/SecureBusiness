package com.marcus.securebusiness.controller;

import com.marcus.securebusiness.dto.UserDTO;
import com.marcus.securebusiness.event.NewUserEvent;
import com.marcus.securebusiness.exception.ApiException;
import com.marcus.securebusiness.form.LoginForm;
import com.marcus.securebusiness.form.SettingForm;
import com.marcus.securebusiness.form.UpdateForm;
import com.marcus.securebusiness.form.UpdatePasswordForm;
import com.marcus.securebusiness.model.HttpResponse;
import com.marcus.securebusiness.model.User;
import com.marcus.securebusiness.model.UserPrincipal;
import com.marcus.securebusiness.provider.TokenProvider;
import com.marcus.securebusiness.service.EventService;
import com.marcus.securebusiness.service.RoleService;
import com.marcus.securebusiness.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.marcus.securebusiness.dtoMapper.UserDTOMapper.toUser;
import static com.marcus.securebusiness.enumeration.EventType.*;
import static com.marcus.securebusiness.util.ExceptionUtils.processError;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RoleService roleService;
    private final EventService eventService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ApplicationEventPublisher publisher;
    private static final String TOKEN_PREFIX = "Bearer ";

    // BEGIN - User register

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user) {
        UserDTO userDTO = userService.createUser(user);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO))
                        .message("User created")
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build());
    }

    // END - User register
    // BEGIN - Login user and retrieve profile

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid LoginForm loginForm) {
        UserDTO userDTO = authenticate(loginForm.getEmail(), loginForm.getPassword());
        // If userDTO.isUsingMfa() = TRUE, then sendVerificationCode(userDTO)
        // else sendResponse(userDTO)
        return userDTO.isUsingMfa() ? sendVerificationCode(userDTO) : sendResponse(userDTO);
    }

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication) {
        UserDTO userDTO = userService.getUser(getEmail(authentication.getName()));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO, "roles", roleService.getRoles(), "events", eventService.getEventByUserId(userDTO.getId())))
                        .message("Profile retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateForm user) {
        UserDTO updateUser = userService.updateUserDetails(user);
        publisher.publishEvent(new NewUserEvent(user.getEmail(), PROFILE_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", updateUser, "roles", roleService.getRoles(), "events", eventService.getEventByUserId(updateUser.getId())))
                        .message("Profile updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
        UserDTO userDTO = userService.verifyCode(email, code);
        publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_SUCCESS));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO, "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDTO))
                                , "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDTO))
                                , "events", eventService.getEventByUserId(userDTO.getId())))
                        .message("Login successful")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    // END - Login user and retrieve profile
    // BEGIN - Login authentication

    private UserDTO getAuthenticatedUser(Authentication authentication) {
        return ((UserDTO) authentication.getPrincipal());
    }

    public static UserDTO getLoggedInUser(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }

    private UserDTO authenticate(String email, String password) {
        if (userService.getUserByEmail(email) != null)
            publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT));
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            UserDTO loggedInUser = getLoggedInUser(authentication);
            if (!loggedInUser.isUsingMfa())
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_SUCCESS));
            return loggedInUser;
        } catch (Exception exception) {
            publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_FAILURE));
            processError(request,response,exception);
            throw new ApiException(exception.getMessage());
        }
    }

    // END - Login authentication
    // BEGIN - Reset password when user not login

    @PatchMapping("/update/password")
    public ResponseEntity<HttpResponse> updatePassword(Authentication authentication, @RequestBody @Valid UpdatePasswordForm form) {
        UserDTO userDTO = getAuthenticatedUser(authentication);
        userService.updatePassword(userDTO.getId(), form.getCurrentPassword(), form.getNewPassword(), form.getConfirmNewPassword());
        publisher.publishEvent(new NewUserEvent(getEmail(authentication.toString()), PASSWORD_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userService.getUserById(userDTO.getId()), "roles", roleService.getRoles(), "events", eventService.getEventByUserId(userDTO.getId())))
                        .message("Password updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) {
        userService.resetPassword(email);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Email sent, please check your email to change the password.")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PostMapping("/resetpassword/{key}/{password}/{confirmPassword}")
    public ResponseEntity<HttpResponse> resetPasswordWithKey(@PathVariable("key") String key,
                                                          @PathVariable("password") String password,
                                                          @PathVariable("confirmPassword") String confirmPassword) {
        userService.renewPassword(key, password, confirmPassword);
        log.info(key);
        publisher.publishEvent(new NewUserEvent(getEmail(key), PASSWORD_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password reset successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordUrl(@PathVariable("key") String key) {
        UserDTO userDTO = userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO,"roles", roleService.getRoles(), "events", eventService.getEventByUserId(userDTO.getId())))
                        .message("Please enter a new password.")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    // END - Reset password when user not login
    // BEGIN - Update roles

    @PatchMapping("/update/role/{roleName}")
    public ResponseEntity<HttpResponse> updateUserRole(Authentication authentication, @PathVariable("roleName") String roleName) {
        UserDTO userDTO = getAuthenticatedUser(authentication);
        userService.updateUserRole(userDTO.getId(), roleName);
        publisher.publishEvent(new NewUserEvent(getEmail(authentication.toString()), ROLE_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(Map.of("user", userService.getUserById(userDTO.getId()), "roles", roleService.getRoles(), "events", eventService.getEventByUserId(userDTO.getId())))
                        .timeStamp(now().toString())
                        .message("Role updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    // End - Update roles
    // BEGIN - Update settings

    @PatchMapping("/update/settings")
    public ResponseEntity<HttpResponse> updateSettings(Authentication authentication, @RequestBody @Valid SettingForm form) {
        UserDTO userDTO = getAuthenticatedUser(authentication);
        userService.updateAccountSettings(userDTO.getId(), form.getEnabled(), form.getNotLocked());
        publisher.publishEvent(new NewUserEvent(getEmail(authentication.toString()), ACCOUNT_SETTING_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(Map.of("user", userService.getUserById(userDTO.getId()), "roles", roleService.getRoles(), "events", eventService.getEventByUserId(userDTO.getId())))
                        .timeStamp(now().toString())
                        .message("Account settings updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/togglemfa")
    public ResponseEntity<HttpResponse> toggleMfa(Authentication authentication) {
        UserDTO userDTO = userService.toggleMfa(getAuthenticatedUser(authentication).getEmail());
        publisher.publishEvent(new NewUserEvent(getEmail(authentication.toString()), MFA_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(Map.of("user", userDTO, "roles", roleService.getRoles(), "events", eventService.getEventByUserId(userDTO.getId())))
                        .timeStamp(now().toString())
                        .message("Multi-Factor Authentication updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/image")
    public ResponseEntity<HttpResponse> updateProfileImage(Authentication authentication, @RequestParam("image") MultipartFile image) {
        UserDTO userDTO =getAuthenticatedUser(authentication);
        userService.updateImage(userDTO, image);
        publisher.publishEvent(new NewUserEvent(getEmail(authentication.toString()), PROFILE_PICTURE_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(Map.of("user", userDTO, "roles", roleService.getRoles(), "events", eventService.getEventByUserId(userDTO.getId())))
                        .timeStamp(now().toString())
                        .message("Profile image updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping(value = "/image/{fileName}", produces = IMAGE_PNG_VALUE)
    public byte[] getProfileImage(@PathVariable("fileName") String fileName) throws Exception {
        return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/Download/images/" + fileName));
    }

    // END - Update settings
    // BEGIN - Verify new account

    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message(userService.verifyAccount(key).isEnabled() ? "Account already verified" : "Account now verified")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    //END - Verify new account
    //BEGIN - Refresh access token
    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request) {
        if (isHeaderTokenValid(request)) {
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDTO userDTO = userService.getUserById(tokenProvider.getSubject(token, request));
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .data(of("user", userDTO, "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDTO))
                                    , "refresh_token", token))
                            .message("Token refresh")
                            .status(OK)
                            .statusCode(OK.value())
                            .build());
        }
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Refresh token not valid or missing")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build());
    }

    //END - Refresh access token
    // BEGIN - Error handler

    @GetMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("There is no Mapping for "+request.getMethod()+"request on this path")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build());
    }

    // END - Error handler
    // BEGIN - Private local methods
    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toString());
    }

    private static String getEmail(String str) {
        String email = "";
        try {
            for (int i=0;i<str.length();i++)
                if (str.substring(i, i + 6).equalsIgnoreCase("email="))
                    for (int j = i + 6; j < str.length(); j++)
                        if (str.substring(j, j + 1).equalsIgnoreCase(",")) {
                            email = str.substring(i + 6, j);
                            return email;
                        }
        } catch (Exception exception) {
            System.out.println("Error occur when go through the String to find email");
            throw exception;
        }
        return email;
    }

    private boolean isHeaderTokenValid(HttpServletRequest request) {
        // check is AUTHORIZATION not null && is header start with TOKEN_PREFIX && isTokenValid
        return request.getHeader(AUTHORIZATION) != null &&
                request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX) &&
                tokenProvider.isTokenValid(tokenProvider.getSubject(request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()), request),
                        request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()));
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDTO userDTO) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO, "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDTO))
                                , "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDTO))))
                        .message("Login successful")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    private UserPrincipal getUserPrincipal(UserDTO userDTO) {
        return new UserPrincipal(toUser(userService.getUser(userDTO.getEmail())), roleService.getRoleByUserId(userDTO.getId()));
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO userDTO) {
        userService.sendVerificationCode(userDTO);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO))
                        .message("SMS VerificationCode Sent")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }
}

// END - Private local methods




















