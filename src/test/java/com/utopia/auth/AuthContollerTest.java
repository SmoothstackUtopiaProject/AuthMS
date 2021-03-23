package com.utopia.auth;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.utopia.auth.controller.AuthController;
import com.utopia.auth.exceptions.ExpiredTokenExpception;
import com.utopia.auth.exceptions.TokenAlreadyIssuedException;
import com.utopia.auth.exceptions.TokenNotFoundExpection;
import com.utopia.auth.exceptions.UserAlreadyExistsException;
import com.utopia.auth.exceptions.UserNotFoundException;
import com.utopia.auth.jwk.JwtTokenProvider;
import com.utopia.auth.models.MailResponse;
import com.utopia.auth.models.Role;
import com.utopia.auth.models.User;
import com.utopia.auth.models.UserToken;
import com.utopia.auth.services.UserService;
import com.utopia.auth.services.UserTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = AuthController.class)
@WebAppConfiguration
@ActiveProfiles("User Controller Test")
public class AuthContollerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private UserTokenService userTokenService;

    @MockBean
    private UsernamePasswordAuthenticationToken authToken;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private User user;
    private UserToken userToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        user = new User(1, Role.USER, "Junit", "Test", "junit@gmail.com", "PasswordUnit", "776565443");
        userToken = new UserToken(user);
    }

    @Test
    void testInsertUserCREATED() throws Exception {
        given(userService.insert(any(User.class))).willAnswer((invocation) -> invocation.getArgument(0));
        mockMvc.perform(
                post("/auth").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.userFirstName", is("Junit")))
                .andExpect(jsonPath("$.userLastName", is("Test")));
    }

    @Test
    void testInsertUserCONFLICT() throws Exception {
        given(userService.insert(any(User.class))).willThrow(UserAlreadyExistsException.class);
        mockMvc.perform(
                post("/auth").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetUserByIdOK() throws Exception {
        given(userService.findById(user.getUserId())).willReturn(user);
        mockMvc.perform(get("/auth/" + user.getUserId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))).andExpect(status().isOk())
                .andExpect(jsonPath("$.userFirstName", is("Junit"))).andExpect(jsonPath("$.userLastName", is("Test")));
        ;
    }

    @Test
    void testGetUserByIdNOTFOUND() throws Exception {
        given(userService.findById(0)).willThrow(UserNotFoundException.class);
        mockMvc.perform(get("/users/" + 0).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))).andExpect(status().isNotFound());
        ;
    }

    @Test
    void testSendRecoveryEmailOK() throws Exception {
        Map<String, String> email = new HashMap<>();
        email.put("userEmail", user.getUserEmail());
        given(userService.sendRecoveryEmail(user.getUserEmail())).willReturn(any(MailResponse.class));
        mockMvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(email))).andExpect(status().isOk());
        ;
    }

    @Test
    void testSendRecoveryEmailNOTFOUND() throws Exception {
        Map<String, String> email = new HashMap<>();
        email.put("userEmail", user.getUserEmail());
        given(userService.sendRecoveryEmail(user.getUserEmail())).willThrow(UserNotFoundException.class);
        mockMvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(email))).andExpect(status().isNotFound());
        ;
    }

    @Test
    void testSendRecoveryEmailCONFLICT() throws Exception {
        Map<String, String> email = new HashMap<>();
        email.put("userEmail", user.getUserEmail());
        given(userService.sendRecoveryEmail(user.getUserEmail())).willThrow(TokenAlreadyIssuedException.class);
        mockMvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(email))).andExpect(status().isConflict());
        ;
    }

    @Test
    void testVerifyTokenOK() throws Exception {
        Map<String, String> veriifyToken = new HashMap<>();
        veriifyToken.put("recoveryCode", user.getUserToken());
        given(userTokenService.verifyToken(user.getUserToken())).willReturn(userToken);
        mockMvc.perform(post("/auth/forgot-password/verify-token").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(veriifyToken))).andExpect(status().isOk());
        ;
    }

    @Test
    void testVerifyTokenExpiredToken() throws Exception {
        Map<String, String> veriifyToken = new HashMap<>();
        veriifyToken.put("recoveryCode", user.getUserToken());
        given(userTokenService.verifyToken(user.getUserToken())).willThrow(ExpiredTokenExpception.class);
        mockMvc.perform(post("/auth/forgot-password/verify-token").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(veriifyToken))).andExpect(status().isNotFound());
        ;
    }

    @Test
    void testVerifyTokenExpiredNOTFOUND() throws Exception {
        Map<String, String> veriifyToken = new HashMap<>();
        veriifyToken.put("recoveryCode", user.getUserToken());
        given(userTokenService.verifyToken(user.getUserToken())).willThrow(TokenNotFoundExpection.class);
        mockMvc.perform(post("/auth/forgot-password/verify-token").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(veriifyToken))).andExpect(status().isNotFound());
        ;
    }

    @Test
    void testOnLoginOK() throws Exception {
        authToken = new UsernamePasswordAuthenticationToken(user.getUserEmail(), user.getUserPassword());
        given(userService.findByEmail(authToken.getName())).willReturn(any(User.class));
        user.setUserToken(tokenProvider.generateToken(authToken));
        mockMvc.perform(get("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authToken))).andExpect(status().isOk());
        ;
    }

    @Test

    void testOnLoginUnauthorized() throws Exception {
        authToken = new UsernamePasswordAuthenticationToken(user.getUserEmail(), user.getUserPassword());
        given(userService.findByEmail(authToken.getName())).willThrow(Unauthorized.class);
        mockMvc.perform(get("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authToken))).andExpect(status().isUnauthorized());
        ;
    }

    @Test
    void testDeleteUserByIdOK() throws Exception {
        mockMvc.perform(delete("/auth/" + user.getUserId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
