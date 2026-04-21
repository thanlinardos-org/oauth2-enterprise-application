package com.thanlinardos.resource_server.model.mapped;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanlinardos.resource_server.model.OwnedResource;
import com.thanlinardos.resource_server.model.entity.account.AccountJpa;
import com.thanlinardos.resource_server.model.entity.account.AccountTransactionJpa;
import com.thanlinardos.resource_server.model.entity.account.CardJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class AccountModel extends BasicIdModel<AccountJpa, AccountModel> implements Serializable, OwnedResource<AccountModel> {

    @ToString.Exclude
    @JsonIgnore
    private OwnerModel owner;
    @Builder.Default
    private List<AccountTransactionModel> accountTransactions = new ArrayList<>();
    private Long accountNumber;
    private String accountType;
    private String branchAddress;
    @Builder.Default
    private List<CardModel> cards = new ArrayList<>();

    public AccountModel(AccountJpa entity) {
        super(entity);
        this.setAccountNumber(entity.getAccountNumber());
        this.setAccountType(entity.getAccountType());
        this.setBranchAddress(entity.getBranchAddress());
        this.setOwner(new OwnerModel(entity.getOwner()));
    }

    /**
     * Sets the accountTransactions list by converting a list of AccountTransactionJpa entities to AccountTransactionModel.
     *
     * @param entities the list of AccountTransactionJpa entities to be converted and set.
     */
    public void setAccountTransactionJpas(List<AccountTransactionJpa> entities) {
        this.setAccountTransactions(entities.stream()
                .map(AccountTransactionModel::new)
                .toList());
    }

    /**
     * Sets the cards list by converting a list of CardJpa entities to CardModel.
     *
     * @param entities the list of CardJpa entities to be converted and set.
     */
    public void setCardJpas(List<CardJpa> entities) {
        this.setCards(entities.stream()
                .map(CardModel::new)
                .toList());
    }

    @Override
    public AccountJpa toEntityOnlyId() {
        return AccountJpa.builder().id(getId()).build(); //NOSONAR (S3252)
    }

    public AccountJpa toEntity() {
        return AccountJpa.builder() //NOSONAR (S3252)
                .accountNumber(getAccountNumber())
                .accountType(getAccountType())
                .branchAddress(getBranchAddress())
                .accountTransactions(getAccountTransactions().stream()
                        .map(AccountTransactionModel::toEntity)
                        .toList())
                .cards(getCards().stream()
                        .map(CardModel::toEntity)
                        .toList())
                .owner(getOwner().toEntity())
                .build();
    }

    @Override
    public AccountModel fromEntity(AccountJpa entity) {
        return new AccountModel(entity);
    }
}
