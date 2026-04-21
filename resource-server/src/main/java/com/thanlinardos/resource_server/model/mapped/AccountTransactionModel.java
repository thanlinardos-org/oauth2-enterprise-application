package com.thanlinardos.resource_server.model.mapped;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanlinardos.resource_server.model.OwnedResource;
import com.thanlinardos.resource_server.model.entity.account.AccountTransactionJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class AccountTransactionModel extends BasicIdModel<AccountTransactionJpa, AccountTransactionModel> implements Serializable, OwnedResource<AccountTransactionModel> {

    private UUID transactionId;
    private Long accountId;
    private LocalDate transactionDt;
    private String transactionSummary;
    private String transactionType;
    private Long transactionAmt;
    private Long closingBalance;
    @ToString.Exclude
    @JsonIgnore
    private OwnerModel owner;

    public AccountTransactionModel(AccountTransactionJpa entity) {
        super(entity);
        this.transactionId = entity.getTransactionId();
        this.transactionDt = entity.getTransactionDt();
        this.transactionSummary = entity.getTransactionSummary();
        this.transactionType = entity.getTransactionType();
        this.transactionAmt = entity.getTransactionAmt();
        this.closingBalance = entity.getClosingBalance();
        this.accountId = entity.getAccount().getId();
        this.owner = new OwnerModel(entity.getAccount().getOwner());
    }

    @Override
    public AccountTransactionJpa toEntityOnlyId() {
        return AccountTransactionJpa.builder().id(getId()).build(); //NOSONAR (S3252)
    }

    public AccountTransactionJpa toEntity() {
        return AccountTransactionJpa.builder() //NOSONAR (S3252)
                .transactionId(getTransactionId())
                .transactionDt(getTransactionDt())
                .transactionSummary(getTransactionSummary())
                .transactionType(getTransactionType())
                .transactionAmt(getTransactionAmt())
                .closingBalance(getClosingBalance())
                .owner(getOwner().toEntity())
                .build();
    }

    @Override
    public AccountTransactionModel fromEntity(AccountTransactionJpa entity) {
        return new AccountTransactionModel(entity);
    }
}
