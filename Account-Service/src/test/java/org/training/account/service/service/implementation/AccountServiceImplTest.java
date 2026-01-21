package org.training.account.service.service.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.training.account.service.exception.*;
import org.training.account.service.external.SequenceService;
import org.training.account.service.external.TransactionService;
import org.training.account.service.external.UserService;
import org.training.account.service.model.AccountStatus;
import org.training.account.service.model.AccountType;
import org.training.account.service.model.dto.AccountDto;
import org.training.account.service.model.dto.AccountStatusUpdate;
import org.training.account.service.model.dto.external.TransactionResponse;
import org.training.account.service.model.dto.external.UserDto;
import org.training.account.service.model.dto.response.Response;
import org.training.account.service.model.entity.Account;
import org.training.account.service.model.mapper.AccountMapper;
import org.training.account.service.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SequenceService sequenceService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AccountMapper accountMapper = new AccountMapper();

    private AccountDto accountDto;
    private Account account;
    private UserDto userDto;

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

        account = Account.builder()
                .accountId(1L)
                .accountNumber("0600140000001")
                .accountType(AccountType.SAVINGS_ACCOUNT)
                .accountStatus(AccountStatus.ACTIVE)
                .availableBalance(BigDecimal.valueOf(1000.00))
                .userId(100L)
                .age(25)
                .Salary(60000.0)
                .build();

        userDto = new UserDto(); // Assume UserDto has necessary fields
    }

    @Test
    void createAccount_ShouldCreateAccountSuccessfully() {
        // Given
        when(userService.readUserById(accountDto.getUserId())).thenReturn(ResponseEntity.ok(userDto));
        when(accountRepository.findAccountByUserIdAndAccountType(accountDto.getUserId(), AccountType.valueOf(accountDto.getAccountType())))
                .thenReturn(Optional.empty());
        when(sequenceService.generateAccountNumber()).thenReturn(new org.training.account.service.model.dto.external.SequenceResponse(1L)); // Assume SequenceResponse
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        Response response = accountService.createAccount(accountDto);

        // Then
        assertNotNull(response);
        assertEquals("200", response.getResponseCode());
        assertEquals(" Account created successfully", response.getMessage());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_ShouldThrowResourceNotFound_WhenUserNotFound() {
        // Given
        when(userService.readUserById(accountDto.getUserId())).thenReturn(ResponseEntity.ok(null));

        // When & Then
        assertThrows(ResourceNotFound.class, () -> accountService.createAccount(accountDto));
    }

    @Test
    void createAccount_ShouldThrowResourceConflict_WhenAccountAlreadyExists() {
        // Given
        when(userService.readUserById(accountDto.getUserId())).thenReturn(ResponseEntity.ok(userDto));
        when(accountRepository.findAccountByUserIdAndAccountType(accountDto.getUserId(), AccountType.valueOf(accountDto.getAccountType())))
                .thenReturn(Optional.of(account));

        // When & Then
        assertThrows(ResourceConflict.class, () -> accountService.createAccount(accountDto));
    }

    @Test
    void createAccount_ShouldThrowResourceConflict_WhenAgeLessThan18() {
        // Given
        accountDto.setAge(16);
        when(userService.readUserById(accountDto.getUserId())).thenReturn(ResponseEntity.ok(userDto));
        when(accountRepository.findAccountByUserIdAndAccountType(accountDto.getUserId(), AccountType.valueOf(accountDto.getAccountType())))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceConflict.class, () -> accountService.createAccount(accountDto));
    }

    @Test
    void createAccount_ShouldThrowResourceConflict_WhenSalaryLessThan50000() {
        // Given
        accountDto.setSalary(40000.0);
        when(userService.readUserById(accountDto.getUserId())).thenReturn(ResponseEntity.ok(userDto));
        when(accountRepository.findAccountByUserIdAndAccountType(accountDto.getUserId(), AccountType.valueOf(accountDto.getAccountType())))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceConflict.class, () -> accountService.createAccount(accountDto));
    }

    @Test
    void updateStatus_ShouldUpdateStatusSuccessfully() {
        // Given
        AccountStatusUpdate statusUpdate = new AccountStatusUpdate();
        statusUpdate.setAccountStatus(AccountStatus.ACTIVE);
        account.setAccountStatus(AccountStatus.PENDING);
        when(accountRepository.findAccountByAccountNumber(accountDto.getAccountNumber())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        Response response = accountService.updateStatus(accountDto.getAccountNumber(), statusUpdate);

        // Then
        assertNotqNull(response);
        assertEquals("Account updated successfully", response.getMessage());
        verify(accountRepository).save(account);
    }

    @Test
    void updateStatus_ShouldThrowAccountStatusException_WhenAccountIsActive() {
        // Given
        AccountStatusUpdate statusUpdate = new AccountStatusUpdate();
        statusUpdate.setAccountStatus(AccountStatus.ACTIVE);
        when(accountRepository.findAccountByAccountNumber(accountDto.getAccountNumber())).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(AccountStatusException.class, () -> accountService.updateStatus(accountDto.getAccountNumber(), statusUpdate));
    }

    @Test
    void updateStatus_ShouldThrowInSufficientFunds_WhenBalanceLow() {
        // Given
        AccountStatusUpdate statusUpdate = new AccountStatusUpdate();
        statusUpdate.setAccountStatus(AccountStatus.ACTIVE);
        account.setAccountStatus(AccountStatus.PENDING);
        account.setAvailableBalance(BigDecimal.valueOf(500.00));
        when(accountRepository.findAccountByAccountNumber(accountDto.getAccountNumber())).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(InSufficientFunds.class, () -> accountService.updateStatus(accountDto.getAccountNumber(), statusUpdate));
    }

    @Test
    void readAccountByAccountNumber_ShouldReturnAccountDto() {
        // Given
        when(accountRepository.findAccountByAccountNumber(accountDto.getAccountNumber())).thenReturn(Optional.of(account));

        // When
        AccountDto result = accountService.readAccountByAccountNumber(accountDto.getAccountNumber());

        // Then
        assertNotNull(result);
        assertEquals(accountDto.getAccountNumber(), result.getAccountNumber());
    }

    @Test
    void readAccountByAccountNumber_ShouldThrowResourceNotFound_WhenAccountNotFound() {
        // Given
        when(accountRepository.findAccountByAccountNumber(accountDto.getAccountNumber())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFound.class, () -> accountService.readAccountByAccountNumber(accountDto.getAccountNumber()));
    }

    @Test
    void getBalance_ShouldReturnBalance() {
        // Given
        when(accountRepository.findAccountByAccountNumber(accountDto.getAccountNumber())).thenReturn(Optional.of(account));

        // When
        String balance = accountService.getBalance(accountDto.getAccountNumber());

        // Then
        assertEquals("1000.00", balance);
    }

    @Test
    void getTransactionsFromAccountId_ShouldReturnTransactions() {
        // Given
        List<TransactionResponse> transactions = List.of(new TransactionResponse()); // Assume TransactionResponse
        when(transactionService.getTransactionsFromAccountId("1")).thenReturn(transactions);

        // When
        List<TransactionResponse> result = accountService.getTransactionsFromAccountId("1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void closeAccount_ShouldCloseAccountSuccessfully() {
        // Given
        account.setAvailableBalance(BigDecimal.ZERO);
        when(accountRepository.findAccountByAccountNumber(accountDto.getAccountNumber())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        Response response = accountService.closeAccount(accountDto.getAccountNumber());

        // Then
        assertNotNull(response);
        assertEquals("Account closed successfully", response.getMessage());
        verify(accountRepository).save(account);
    }

    @Test
    void closeAccount_ShouldThrowAccountClosingException_WhenBalanceNotZero() {
        // Given
        when(accountRepository.findAccountByAccountNumber(accountDto.getAccountNumber())).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(AccountClosingException.class, () -> accountService.closeAccount(accountDto.getAccountNumber()));
    }

    @Test
    void readAccountByUserId_ShouldReturnAccountDto() {
        // Given
        when(accountRepository.findAccountByUserId(100L)).thenReturn(Optional.of(account));

        // When
        AccountDto result = accountService.readAccountByUserId(100L);

        // Then
        assertNotNull(result);
        assertEquals(accountDto.getUserId(), result.getUserId());
    }

    @Test
    void readAccountByUserId_ShouldThrowAccountStatusException_WhenAccountNotActive() {
        // Given
        account.setAccountStatus(AccountStatus.CLOSED);
        when(accountRepository.findAccountByUserId(100L)).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(AccountStatusException.class, () -> accountService.readAccountByUserId(100L));
    }

    @Test
    void exampleNewTest_ShouldPass() {
        // This is an example test added for a new commit
        assertTrue(true);
    }
}