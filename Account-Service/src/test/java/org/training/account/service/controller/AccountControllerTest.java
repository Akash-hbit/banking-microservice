package org.training.account.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.training.account.service.model.dto.AccountDto;
import org.training.account.service.model.dto.AccountStatusUpdate;
import org.training.account.service.model.dto.external.TransactionResponse;
import org.training.account.service.model.dto.response.Response;
import org.training.account.service.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountDto accountDto;
    private Response response;

    @BeforeEach
    void setUp() {
        accountDto = AccountDto.builder()
                .accountId(1L)
                .accountNumber("0600140000001")
                .accountType("SAVINGS_ACCOUNT")
                .accountStatus("ACTIVE")
                .availableBalance(BigDecimal.valueOf(1000.00))
                .userId(100L)
                .age(25)
                .Salary(60000.0)
                .build();

        response = Response.builder()
                .responseCode("200")
                .message("Success")
                .build();
    }

    @Test
    void createAccount_ShouldReturnCreated() throws Exception {
        // Given
        when(accountService.createAccount(any(AccountDto.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseCode").value("200"))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void updateAccountStatus_ShouldReturnOk() throws Exception {
        // Given
        AccountStatusUpdate statusUpdate = new AccountStatusUpdate();
        when(accountService.updateStatus(anyString(), any(AccountStatusUpdate.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/accounts")
                        .param("accountNumber", "0600140000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("200"));
    }

    @Test
    void readByAccountNumber_ShouldReturnAccountDto() throws Exception {
        // Given
        when(accountService.readAccountByAccountNumber("0600140000001")).thenReturn(accountDto);

        // When & Then
        mockMvc.perform(get("/accounts")
                        .param("accountNumber", "0600140000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("0600140000001"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS_ACCOUNT"));
    }

    @Test
    void updateAccount_ShouldReturnOk() throws Exception {
        // Given
        when(accountService.updateAccount(anyString(), any(AccountDto.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/accounts")
                        .param("accountNumber", "0600140000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void accountBalance_ShouldReturnBalance() throws Exception {
        // Given
        when(accountService.getBalance("0600140000001")).thenReturn("1000.00");

        // When & Then
        mockMvc.perform(get("/accounts/balance")
                        .param("accountNumber", "0600140000001"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.00"));
    }

    @Test
    void getTransactionsFromAccountId_ShouldReturnTransactions() throws Exception {
        // Given
        List<TransactionResponse> transactions = List.of(new TransactionResponse()); // Assume structure
        when(accountService.getTransactionsFromAccountId("1")).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/accounts/{accountId}/transactions", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void closeAccount_ShouldReturnOk() throws Exception {
        // Given
        when(accountService.closeAccount("0600140000001")).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/accounts/closure")
                        .param("accountNumber", "0600140000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void readAccountByUserId_ShouldReturnAccountDto() throws Exception {
        // Given
        when(accountService.readAccountByUserId(100L)).thenReturn(accountDto);

        // When & Then
        mockMvc.perform(get("/accounts/{userId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(100));
    }

    @Test
    void ping_ShouldReturnOk() throws Exception {
        // When & Then
        mockMvc.perform(get("/accounts/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account Service is up and running"));
    }
}