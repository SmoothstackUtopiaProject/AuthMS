package com.utopia.auth.controller;

import java.net.ConnectException;
import java.security.Principal;
import java.sql.SQLException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utopia.auth.exceptions.UserAlreadyExistsException;
import com.utopia.auth.exceptions.UserNotFoundException;
import com.utopia.auth.jwk.JwtTokenProvider;
import com.utopia.auth.models.HttpError;
import com.utopia.auth.models.Role;
import com.utopia.auth.models.User;
import com.utopia.auth.services.UserService;


@RestController
@CrossOrigin
@RequestMapping("/auth")
public class AuthController {

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private UserService userService;
  
  @PostMapping
  public ResponseEntity<Object> insert(@Valid @RequestBody User user) {
	  
    try {
      user.setUserRole(Role.USER);
      return new ResponseEntity<>(userService.insert(user), HttpStatus.CREATED);
    } catch (UserAlreadyExistsException err) {
      return new ResponseEntity<>(new HttpError(err.getMessage(), 409), HttpStatus.CONFLICT
      );
    }
  }

  @GetMapping("/login")
  public ResponseEntity<Object> login(Principal principal) throws UserNotFoundException{
    if (principal == null) {
      return ResponseEntity.ok(principal);
    }
    UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
    User user = userService.findByEmail(authenticationToken.getName());
    user.setUserToken(tokenProvider.generateToken(authenticationToken));

    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @DeleteMapping("{userId}")
  public ResponseEntity<Object> delete(@PathVariable Integer userId) {
    try {
      userService.delete(userId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (UserNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Object> invalidUser() {
    return new ResponseEntity<>("Invalid username of", HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ConnectException.class)
  public ResponseEntity<Object> invalidConnection() {
    return new ResponseEntity<>(
      new HttpError(
        "Service temporarily unavailable",
        HttpStatus.SERVICE_UNAVAILABLE.value()
      ),
      HttpStatus.SERVICE_UNAVAILABLE
    );
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> invalidMessage() {
    return new ResponseEntity<>(
      new HttpError("Invalid Message Content!", HttpStatus.BAD_REQUEST.value()),
      HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(SQLException.class)
  public ResponseEntity<Object> invalidSQL() {
    return new ResponseEntity<>(
      new HttpError(
        "Service temporarily unavailable",
        HttpStatus.SERVICE_UNAVAILABLE.value()
      ),
      HttpStatus.SERVICE_UNAVAILABLE
    );
  }
}
