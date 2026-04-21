package com.thanlinardos.resource_server.service.account;

import com.thanlinardos.resource_server.model.entity.account.AccountJpa;
import com.thanlinardos.resource_server.model.info.RegisterAccountDetails;
import com.thanlinardos.resource_server.model.mapped.AccountModel;
import com.thanlinardos.resource_server.repository.api.AccountRepository;
import com.thanlinardos.resource_server.service.owner.OwnerService;
import com.thanlinardos.resource_server.service.user.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserService userService;
    private final OwnerService ownerService;
    private final AccountRepository accountRepository;

    @Transactional
    public AccountModel saveAccount(RegisterAccountDetails accountDetails) {
        AccountJpa savedAccount = accountRepository.save(getAccountFromRegisterDetails(accountDetails));
        return new AccountModel(savedAccount);
    }

    private AccountJpa getAccountFromRegisterDetails(RegisterAccountDetails accountDetails) {
        return AccountJpa.builder() //NOSONAR (S3252)
                .accountNumber(accountDetails.accountNumber())
                .accountType(accountDetails.accountType())
                .branchAddress(accountDetails.branchAddress())
                .owner(ownerService.getOwnerByUsername(accountDetails.username()))
                .build();
    }

    public Optional<AccountModel> getAccountByAccountNumber(long accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(AccountModel::new);
    }

    /**
     * Finds an AccountJpa by the email of its owner.
     *
     * @param email the email of the owner of the account
     * @return an Optional containing the AccountJpa if found, otherwise empty
     */
    private Optional<AccountJpa> getAccountJpaByCustomerEmail(String email) {
        return accountRepository.findByOwnerName(email);
    }

    public Optional<AccountModel> getAccountByCustomerEmail(String email) {
        return getAccountJpaByCustomerEmail(email)
                .map(AccountModel::new);
    }
}
