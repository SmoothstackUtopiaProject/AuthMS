package com.utopia.auth.services;

import com.utopia.auth.controller.AuthController;
import com.utopia.auth.exceptions.IncorrectPasswordException;
import com.utopia.auth.exceptions.PasswordNotAllowedException;
import com.utopia.auth.exceptions.TokenAlreadyIssuedException;
import com.utopia.auth.exceptions.UserAlreadyExistsException;
import com.utopia.auth.exceptions.UserNotFoundException;
import com.utopia.auth.models.MailRequest;
import com.utopia.auth.models.MailResponse;
import com.utopia.auth.models.User;
import com.utopia.auth.models.UserToken;
import com.utopia.auth.repositories.UserRepository;
import com.utopia.auth.repositories.UserTokenRepository;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  UserRepository userRepository;

  @Autowired
  UserTokenRepository userTokenRepository;

  @Autowired
  UserTokenService userTokenService;

  @Autowired
  EmailService emailService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  public User insert(User user) throws UserAlreadyExistsException {
    Optional<User> checkIfEmailExist = userRepository.findByEmail(user.getUserEmail());
    Optional<User> checkIfPhoneExist = userRepository.findByPhone(user.getUserPhone());
    if (checkIfEmailExist.isPresent()) {
      throw new UserAlreadyExistsException("A user with this email already exists!");
    }
    if (checkIfPhoneExist.isPresent()) {
      throw new UserAlreadyExistsException("A user with this phone number already exists!");
    }

    user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
    return userRepository.save(user);
  }

  public User update(Integer id, Map<String, String> userData) throws UserNotFoundException {
    User user = findById(id);
    user.setUserEmail(userData.get("userEmail"));
    user.setUserFirstName(userData.get("userFirstName"));
    user.setUserLastName(userData.get("userLastName"));
    user.setUserPhone(userData.get("userPhone"));
    return userRepository.save(user);
  }

  public void delete(Integer id) throws UserNotFoundException {
    findById(id);
    userRepository.deleteById(id);
  }

  public MailResponse sendRecoveryEmail(String email) throws UserNotFoundException, TokenAlreadyIssuedException {
    User user = findByEmail(email);
    // getting current date and subtracting 15 minutes to check if token already
    // issued
    Date currentDateTimeMinuts15Minutes = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(15));
    boolean userTokens = userTokenService.verifyIfTokenBeenIssuedin15Min(user.getUserId(),
        currentDateTimeMinuts15Minutes);
    if (!userTokens)
      throw new TokenAlreadyIssuedException("You can only request a link once every 15 minutes!");

    // if token has't been issued in the last 15 minutes, issue a token and send an
    // email to user.
    UserToken userToken = new UserToken(user);

    userTokenRepository.save(userToken);
    LOGGER.info("Atempt to send a recovery email...");
    return sendEmail(user, userToken);
  }

  public void ChangePassword(UserToken userToken, String password)
      throws UserNotFoundException, PasswordNotAllowedException {
    User user = findById(userToken.getUser().getUserId());
    if (user.getUserPassword().equals(password))
      throw new PasswordNotAllowedException("Previously used password not allowed");
    user.setUserPassword(passwordEncoder.encode(password));
    userRepository.save(user);
  }

  public MailResponse sendEmail(User user, UserToken userToken) {
    LOGGER.info("Sending recovery eamil to user. email: " + user.getUserEmail());
    Map<String, Object> modelsMap = new HashMap<>();

    String recoveryCode = userToken.getToken();
    String userName = user.getUserFirstName();

    modelsMap.put("name", userName);
    modelsMap.put("confirmation", recoveryCode);

    MailRequest mailRequest = new MailRequest(user.getUserEmail());
    LOGGER.info("Atempt to send a recovery email...");
    return emailService.sendEmail(mailRequest, modelsMap);
  }

  public User verifyUser(String email, String password) throws UserNotFoundException, IncorrectPasswordException {
    Optional<User> checkUser = userRepository.findByEmail(email);

    if (!checkUser.isPresent()) {
      throw new UserNotFoundException("Invalid Email");
    } else if (!checkUser.get().getUserPassword().equals(password)) {
      throw new IncorrectPasswordException("Invalid password");
    } else
      return checkUser.get();
  }

  public User findByEmail(String email) throws UserNotFoundException {
    String formattedEmail = formatGeneric(email);
    Optional<User> optionalUser = userRepository.findByEmail(formattedEmail);
    if (!optionalUser.isPresent())
      throw new UserNotFoundException("No user with email: \"" + email + "\" exist!");
    return optionalUser.get();
  }

  public User findById(Integer id) throws UserNotFoundException {
    Optional<User> optionalUser = userRepository.findById(id);
    if (!optionalUser.isPresent())
      throw new UserNotFoundException("No user with ID: \"" + id + "\" exist!");
    return optionalUser.get();
  }

  public User findByPhone(String phone) throws UserNotFoundException {
    String formattedPhone = formatPhone(phone);

    Optional<User> optionalUser = userRepository.findByPhone(formattedPhone);
    if (!optionalUser.isPresent())
      throw new UserNotFoundException("No user with phone: \"" + phone + "\" exist!");
    return optionalUser.get();
  }

  private String formatGeneric(String name) {
    return name.trim().toUpperCase();
  }

  private String formatPhone(String phone) {
    return phone.replaceAll("[^0-9]", "");
  }
}
