package com.hardcore.accounting.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hardcore.accounting.converter.c2s.UserInfoC2SConverter;
import com.hardcore.accounting.exception.GlobalExceptionHandler;
import com.hardcore.accounting.manager.UserInfoManager;
import com.hardcore.accounting.model.common.UserInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserInfoManager userInfoManager;
    @Mock
    private UserInfoC2SConverter userInfoC2SConverter;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                                 .setControllerAdvice(new GlobalExceptionHandler())
                                 .build();
    }

    @AfterEach
    public void teardown() {
        reset(userInfoManager);
        reset(userInfoC2SConverter);
    }

    @Test
    public void testGetUserInfoByUserId() throws Exception {
        // Arrange
        val userId = 100L;
        val username = "hardcore";
        val password = "hardcore";

        val userInfoInCommon = UserInfo.builder()
                                       .id(userId)
                                       .username(username)
                                       .password(password)
                                       .build();

        val userInfo = com.hardcore.accounting.model.service.UserInfo
                                                            .builder()
                                                            .id(userId)
                                                            .username(username)
                                                            .password(password)
                                                            .build();

        doReturn(userInfoInCommon).when(userInfoManager).getUserInfoByUserId(anyLong());

        doReturn(userInfo).when(userInfoC2SConverter).convert(userInfoInCommon);

        // Act && Assert
        mockMvc.perform(get("/v1.0/users/" + userId)
                            .contentType("application/json"))
               .andExpect(status().isOk())
               .andExpect(content().contentType("application/json"))
               .andExpect(content().string(new ObjectMapper().writeValueAsString(userInfo)));

        verify(userInfoManager).getUserInfoByUserId(anyLong());
        verify(userInfoC2SConverter).convert(userInfoInCommon);
    }

    @Test
    public void testGetUserInfoByUserIdWithInvalidUserId() throws Exception {
        // Arrange
        val userId = -100L;

        // Act && Assert
        mockMvc.perform(get("/v1.0/users/" + userId)
                            .contentType("application/json")
                            .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().is4xxClientError())
               .andExpect(content().contentType("application/json"))
               .andExpect(content().string("{\"code\":\"INVALID_PARAMETER\",\"errorType\":\"Client\",\"message\":\"The user id -100 is invalid\",\"statusCode\":400}"));

        verify(userInfoManager, never()).getUserInfoByUserId(anyLong());
    }

    @Test
    public void testRegister() throws Exception {
        // Arrange
        val userId = 1L;
        val username = "hardcore";
        val password = "hardcore";

        val request = com.hardcore.accounting.model.service.UserInfo.builder()
                                                                    .username(username)
                                                                    .password(password)
                                                                    .build();

        val userInfoInCommon = UserInfo.builder()
                                       .id(userId)
                                       .username(username)
                                       .password(password)
                                       .build();

        val userInfo = com.hardcore.accounting.model.service.UserInfo
                                                            .builder()
                                                            .id(userId)
                                                            .username(username)
                                                            .password(password)
                                                            .build();


        doReturn(userInfoInCommon).when(userInfoManager).register(username, password);
        doReturn(userInfo).when(userInfoC2SConverter).convert(userInfoInCommon);

        // Act && Assert
        mockMvc.perform(post("/v1.0/users")
                            .contentType("application/json")
                            .content(new ObjectMapper().writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentType("application/json"))
               .andExpect(content().string(new ObjectMapper().writeValueAsString(userInfo)));

        verify(userInfoManager).register(username, password);
        verify(userInfoC2SConverter).convert(userInfoInCommon);
    }

}
