package com.marcus.securebusiness.repository.implementation;

import com.marcus.securebusiness.dto.UserDTO;
import com.marcus.securebusiness.enumeration.VerificationType;
import com.marcus.securebusiness.exception.ApiException;
import com.marcus.securebusiness.form.UpdateForm;
import com.marcus.securebusiness.model.Role;
import com.marcus.securebusiness.model.User;
import com.marcus.securebusiness.model.UserPrincipal;
import com.marcus.securebusiness.repository.RoleRepository;
import com.marcus.securebusiness.repository.UserRepository;
import com.marcus.securebusiness.rowMapper.UserRowMapper;
import com.marcus.securebusiness.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.marcus.securebusiness.enumeration.RoleType.ROLE_USER;
import static com.marcus.securebusiness.enumeration.VerificationType.ACCOUNT;
import static com.marcus.securebusiness.enumeration.VerificationType.PASSWORD;
import static com.marcus.securebusiness.query.UserQuery.*;
import static com.marcus.securebusiness.util.SmsUtils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {
    private static final String DATE_FORMAT = "YYYY-MM-dd hh:mm:ss";
    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;

    @Override
    public User create(User user) {
        // Check the email is unique
        if (getEmailCount(user.getEmail().trim().toLowerCase()) > 0)
            throw new ApiException("Email already in use. Please try a different one.");
        // Save new user
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getSqlParameterSource(user);
            jdbc.update(INSERT_USER_QUERY, parameters, holder);
            user.setId(requireNonNull(holder.getKey()).longValue());
            // Add role to the user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());
            // Send verification URL
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            log.info("verification Url: {}", verificationUrl);
            // Save URL in verification table
            jdbc.update(INSERT_VERIFICATION_QUERY, Map.of("userId", user.getId(), "url",verificationUrl));
            // Send email to user with verification URL
            sendEmail(user.getFirstName(), user.getEmail(), changeToFront(verificationUrl), ACCOUNT);
            //emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);
            user.setEnabled(false);
            user.setNotLocked(true);
            // Return the newly created user
            return user;
            // If any error, throw exception with proper message
        }  catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again later");
        }
    }

    private String changeToFront(String verificationUrl) {
        String newUrl = "";
        for (int i=0; i<verificationUrl.length(); i++) {
            try {
                if (verificationUrl.substring(i, i+1).equals("8") &&
                        verificationUrl.substring(i+1, i+2).equals("0") &&
                        verificationUrl.substring(i+2, i+3).equals("8") &&
                        verificationUrl.substring(i+3, i+4).equals("0")) {
                    newUrl = verificationUrl.substring(0, i) + "4200" + verificationUrl.substring(i+4);
                }
            } catch (Exception exception) {throw new ApiException(exception.getMessage());}
        }
        return newUrl;
    }

    private void sendEmail(String firstName, String email, String verificationUrl, VerificationType verificationType) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try { emailService.sendVerificationEmail(firstName, email, verificationUrl, verificationType); }
                catch (Exception exception) { throw new ApiException("An error occur while sending email in a different thread"); }
            }
        });
    }


    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        try {
            return jdbc.queryForObject(SELECT_USER_BY_ID_QUERY, Map.of("id", id), new UserRowMapper());
            // If any error, throw exception with proper message
        }  catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No user found by ID: " + id);
        }  catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred during get user by id.");
        }
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encoder.encode(user.getPassword()));
    }

    private SqlParameterSource getUserSqlParameterSource(UpdateForm user) {
        return new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("phone", user.getPhone())
                .addValue("address", user.getAddress())
                .addValue("title", user.getTitle())
                .addValue("bio", user.getBio());
    }

    private String getVerificationUrl(String key, String type) {
        return fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();
        //return ("http://localhost:4200/user/verify/" + type + "/" + key);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if (user == null) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found in the database");
        } else {
            // log.info("User found in the database: {}", email);
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()));
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
            return user;
            // If any error, throw exception with proper message
        }  catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No user found by email: " + email);
        }  catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred during get user by email.");
        }
    }

    @Override
    public User getUserById(Long userId) {
        try {
            return jdbc.queryForObject(SELECT_USER_BY_ID_QUERY, Map.of("id", userId), new UserRowMapper());
            // If any error, throw exception with proper message
        }  catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No user found by id: " + userId);
        }  catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred during get user by id.");
        }
    }

    @Override
    public void updatePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword) {
        try {
            if (!newPassword.equals(confirmNewPassword)) throw new ApiException("Two password must be the same!");
            User user = get(userId);
            if (!encoder.matches(currentPassword, user.getPassword())) throw new ApiException("The current password is incorrect!");
            jdbc.update(UPDATE_USER_PASSWORD_BY_ID_QUERY, Map.of("id", userId, "password", encoder.encode(newPassword)));
            // If any error, throw exception with proper message
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred during update password.");
        }
    }

    @Override
    public void updatePassword(Long userId, String newPassword, String confirmNewPassword) {
        try {
            if (!newPassword.equals(confirmNewPassword)) throw new ApiException("Two password must be the same!");
            User user = get(userId);
            jdbc.update(UPDATE_USER_PASSWORD_BY_ID_QUERY, Map.of("id", userId, "password", encoder.encode(newPassword)));
            jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, Map.of("userId", userId));
            // If any error, throw exception with proper message
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        }
    }

    @Override
    public void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked) {
        try {
            jdbc.update(UPDATE_USER_SETTINGS_BY_ID_QUERY, Map.of("id", userId, "enabled", enabled, "nonLocked", notLocked));
            // If any error, throw exception with proper message
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred during update account settings.");
        }
    }

    @Override
    public User toggleMfa(String email) {
        User user = getUserByEmail(email);
        if(isBlank(user.getPhone())) throw new ApiException("You need a phone number first.");
        user.setUsingMfa(!user.isUsingMfa());
        try {
            jdbc.update(TOGGLE_USER_MFA_QUERY, Map.of("email", email, "isUsingMfa", user.isUsingMfa()));
            return user;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occur during toggle mfa");
        }
    }

    @Override
    public void updateImage(UserDTO userDTO, MultipartFile image) {
        String userImageUrl = setUserImageUrl(userDTO.getEmail());
        userDTO.setImageUrl(userImageUrl);
        saveImage(userDTO.getEmail(), image);
        jdbc.update(UPDATE_USER_IMAGE_QUERY, Map.of("imageUrl", userImageUrl, "id", userDTO.getId()));
    }

    private String setUserImageUrl(String email) {
        return fromCurrentContextPath().path("/user/image/" + email + ".png").toUriString();
    }

    private void saveImage(String email, MultipartFile image) {
        Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Download/images/").toAbsolutePath().normalize();
        if (!Files.exists(fileStorageLocation)) {
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException("Unable to create directories to save image");
            }
            log.info("Created directories at: {}", fileStorageLocation);
        }
        try {
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(email + ".png"), REPLACE_EXISTING);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        }
        log.info("File saved in: {} Folder", fileStorageLocation);
    }

    @Override
    public void sendVerificationCode(UserDTO userDTO) {
        String expirationDate = DateFormatUtils.format(addDays(new Date(), 2), DATE_FORMAT);
        String verificationCode = randomAlphabetic(6).toUpperCase();
        try {
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID, Map.of("id", userDTO.getId()));
            jdbc.update(INSERT_VERIFICATION_CODE_QUERY, Map.of("userId", userDTO.getId(), "code", verificationCode, "expirationDate", expirationDate));
            newThreadSMS(userDTO.getPhone(), "From: SecureBusiness \nVerification Code\n" + verificationCode);
            log.info("Verification Code: "  + verificationCode);
            // If any error, throw exception with proper message
        }  catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred during the jdbc.update() in 'UserRepositoryImpl.class' Please try again later");
        }
    }

    private void newThreadSMS(String phone, String s) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try { sendSMS(phone, s); }
                catch (Exception exception) { throw new ApiException("An error occur while sending email in a different thread"); }
            }
        });
    }

    @Override
    public User verifyCode(String email, String code) {
        if (isVerificationCodeExpired(code))
            throw new ApiException("The verification code is expired. Please log in again.");
        try {
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY, Map.of("code", code), new UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
            if (userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                jdbc.update(DELETE_CODE, Map.of("code", code));
                return userByCode;
            } else {
                throw new ApiException("Code is invalid, please try again");
            }
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("Unable to find record");
        } catch (Exception exception) {
            throw new ApiException("An error occur during verify the code");
        }
    }

    @Override
    public void resetPassword(String email) {
        if (getEmailCount(email.trim().toLowerCase()) < 1) throw new ApiException("Email does not exist");
        try {
                String expirationDate = DateFormatUtils.format(addDays(new Date(), 2), DATE_FORMAT);
                User user= getUserByEmail(email);
                String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
                jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, Map.of("userId", user.getId()));
                jdbc.update(INSERT_PASSWORD_VERIFIACTION_QUERY, Map.of("userId", user.getId(), "url", verificationUrl, "expirationDate", expirationDate));
                // TODO Send Email Notification
                log.info("verification Url: {}", verificationUrl);
                sendEmail(user.getFirstName(), user.getEmail(), changeToFront(verificationUrl), PASSWORD);
            }
        catch (Exception exception) {
            throw new ApiException("An error occur during reset password");
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if (isLinkExpired(key, PASSWORD)) throw new ApiException("The link is expired, please reset your password again");
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY, Map.of("url", getVerificationUrl(key, PASSWORD.getType())), new UserRowMapper());
            // jdbc.update(DELETE_USER_FROM_PASSWORD_VERIFICATION_QUERY, Map.of("id", user.getId()));
            // log.info("Deleted the link");
            return user;
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occur during verify Password Key");
        }
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) throw new ApiException("Two password must be the same!");
        try {
            jdbc.update(UPDATE_USER_PASSWORD_BY_URL_QUERY, Map.of("url", getVerificationUrl(key, PASSWORD.getType()), "password", encoder.encode(password)));
            jdbc.update(DELETE_VERIFICATION_BY_URL_QUERY, Map.of("url", getVerificationUrl(key, PASSWORD.getType())));
        } catch (Exception exception) {
            throw new ApiException("An error occur during renewing the password");
        }
    }

    @Override
    public User verifyAccount(String key) {
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_ACCOUNT_KEY_QUERY, Map.of("key", getVerificationUrl(key, ACCOUNT.getType())), new UserRowMapper());
            jdbc.update(UPDATE_USER_ENABLED_QUERY, Map.of("enabled", true, "id", user.getId()));
            return user;
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("Unable to find record");
        } catch (Exception exception) {
            throw new ApiException("An error occur during verify the account");
        }
    }

    @Override
    public User updateUserDetails(UpdateForm user) {
        try {
            jdbc.update(UPDATE_USER_DETAILS_QUERY, getUserSqlParameterSource(user));
            return get(user.getId());
        } catch (Exception exception) {
            throw new ApiException("Error occur during update user details");
        }
    }

    private boolean isLinkExpired(String key, VerificationType password) {
        try {
            return jdbc.queryForObject(SELECT_EXPIRATION_BY_URL, Map.of("url", getVerificationUrl(key, password.getType())), Boolean.class);
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occur during check whether the link expired");
        }
    }

    private boolean isVerificationCodeExpired(String code) {
        try {
            return jdbc.queryForObject(SELECT_CODE_EXPIRATION_QUERY, Map.of("code", code), Boolean.class);
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("Unable to find record");
        } catch (Exception exception) {
            throw new ApiException("An error occur during check the expiration date");
        }
    }
}










