package com.utopia.auth.controller;

import com.utopia.auth.exceptions.ExpiredTokenExpception;
import com.utopia.auth.exceptions.PasswordNotAllowedException;
import com.utopia.auth.exceptions.TokenAlreadyIssuedException;
import com.utopia.auth.exceptions.TokenNotFoundExpection;
import com.utopia.auth.exceptions.UserAlreadyExistsException;
import com.utopia.auth.exceptions.UserNotFoundException;
import com.utopia.auth.jwk.JwtTokenProvider;
import com.utopia.auth.models.Role;
import com.utopia.auth.models.User;
import com.utopia.auth.services.UserService;
import com.utopia.auth.services.UserTokenService;
import java.security.Principal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private UserService userService;

  @Autowired
  UserTokenService userTokenService;

  private static final Logger LOGGER = LoggerFactory.getLogger(
    AuthController.class
  );

  @PostMapping
  public ResponseEntity<Object> insert(@RequestBody User user)
    throws UserAlreadyExistsException {
    LOGGER.info("POST new user");
    user.setUserRole(Role.USER);
    return new ResponseEntity<>(userService.insert(user), HttpStatus.CREATED);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<Object> findById(@PathVariable Integer userId)
    throws UserNotFoundException {
    LOGGER.info("GET user with ID: " + userId);
    User user = userService.findById(userId);
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @GetMapping("/login")
  public ResponseEntity<Object> login(Principal principal)
    throws UserNotFoundException {
    LOGGER.info("Login user");
    if (principal == null) {
      return ResponseEntity.ok(principal);
    }
    UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
    User user = userService.findByEmail(authenticationToken.getName());
    user.setUserToken(tokenProvider.generateToken(authenticationToken));

    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Object> forgotPassword(
    @RequestBody Map<String, String> uMap
  )
    throws UserNotFoundException, TokenAlreadyIssuedException {
    LOGGER.info("Login user");
    String email = uMap.get("userEmail");
    userService.sendRecoveryEmail(email);
    return new ResponseEntity<>(null, HttpStatus.OK);
  }

  @PostMapping("/forgot-password/verify-token")
  public ResponseEntity<Object> verifyToken(
    @RequestBody Map<String, String> uMap
  )
    throws ExpiredTokenExpception, TokenNotFoundExpection {
    LOGGER.info("Verify token");
    String recoveryCode = uMap.get("recoveryCode");
    userTokenService.verifyToken(recoveryCode);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/forgot-password/recover")
  public ResponseEntity<Object> passwordRecovery(
    @RequestBody Map<String, String> uMap
  )
    throws UserNotFoundException, PasswordNotAllowedException, ExpiredTokenExpception, TokenNotFoundExpection {
    LOGGER.info("Change password request");
    String recoveryCode = uMap.get("recoveryCode");
    String password = uMap.get("password");
    userService.ChangePassword(
      userTokenService.verifyToken(recoveryCode),
      password
    );
    userTokenService.delete(recoveryCode);
    return new ResponseEntity<>(
      "Password successfully changed ",
      HttpStatus.OK
    );
  }

  @PutMapping("{userId}")
  public ResponseEntity<Object> update(
    @PathVariable Integer userId,
    @RequestBody Map<String, String> userData
  )
    throws UserNotFoundException {
    LOGGER.info("Update user id: " + userId);
    User user = userService.update(userId, userData);
    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
      user.getUserEmail(),
      user.getUserPassword()
    );
    user.setUserToken(tokenProvider.generateToken(authenticationToken));
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @DeleteMapping("{userId}")
  public ResponseEntity<Object> delete(@PathVariable Integer userId)
    throws UserNotFoundException {
    LOGGER.info("DELETE user id " + userId);
    userService.delete(userId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
