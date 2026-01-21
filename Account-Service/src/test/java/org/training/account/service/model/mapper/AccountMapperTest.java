package org.training.account.service.model.mapper;

import org.junit.jupiter.api.Test;
import org.training.account.service.model.AccountStatus;
import org.training.account.service.model.AccountType;
import org.training.account.service.model.dto.AccountDto;
import org.training.account.service.model.entity.Account;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapper();

    @Test
    void convertToEntity_ShouldMapDtoToEntity() {
        // Given
        AccountDto dto = AccountDto.builder()
                .accountId(1L)
                .accountNumber("123456")
                .accountType("SAVINGS_ACCOUNT")
                .accountStatus("ACTIVE")
                .availableBalance(BigDecimal.valueOf(1000.00))
                .userId(100L)
                .age(30)
                .Salary(50000.0)
                .build();

        // When
        Account entity = mapper.convertToEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(dto.getAccountId(), entity.getAccountId());
        assertEquals(dto.getAccountNumber(), entity.getAccountNumber());
        assertEquals(dto.getAccountType(), entity.getAccountType().name());
        assertEquals(dto.getAccountStatus(), entity.getAccountStatus().name());
        assertEquals(dto.getAvailableBalance(), entity.getAvailableBalance());
        assertEquals(dto.getUserId(), entity.getUserId());
        assertEquals(dto.getAge(), entity.getAge());
        assertEquals(dto.getSalary(), entity.getSalary());
    }

    @Test
    void convertToEntity_ShouldReturnEmptyEntityWhenDtoIsNull() {
        // When
        Account entity = mapper.convertToEntity(null);

        // Then
        assertNotNull(entity);
        assertNull(entity.getAccountId());
    }

    @Test
    void convertToDto_ShouldMapEntityToDto() {
        // Given
        Account entity = Account.builder()
                .accountId(1L)
                .accountNumber("123456")
                .accountType(AccountType.SAVINGS_ACCOUNT)
                .accountStatus(AccountStatus.ACTIVE)
                .openingDate(LocalDate.now())
                .availableBalance(BigDecimal.valueOf(1000.00))
                .userId(100L)
                .age(30)
                .Salary(50000.0)
                .build();

        // When
        AccountDto dto = mapper.convertToDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals(entity.getAccountId(), dto.getAccountId());
        assertEquals(entity.getAccountNumber(), dto.getAccountNumber());
        assertEquals(entity.getAccountType().name(), dto.getAccountType());
        assertEquals(entity.getAccountStatus().name(), dto.getAccountStatus());
        assertEquals(entity.getAvailableBalance(), dto.getAvailableBalance());
        assertEquals(entity.getUserId(), dto.getUserId());
        assertEquals(entity.getAge(), dto.getAge());
        assertEquals(entity.getSalary(), dto.getSalary());
    }

    @Test
    void convertToDto_ShouldReturnEmptyDtoWhenEntityIsNull() {
        // When
        AccountDto dto = mapper.convertToDto(null);

        // Then
        assertNotNull(dto);
        assertNull(dto.getAccountId());
    }
}