package com.thanlinardos.resource_server.model.mapped;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanlinardos.resource_server.model.OwnedResource;
import com.thanlinardos.resource_server.model.entity.account.CardJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

import static com.thanlinardos.spring_enterprise_library.spring_cloud_security.utils.EntityUtils.buildEntityWithIdOrNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class CardModel extends BasicIdModel<CardJpa, CardModel> implements Serializable, OwnedResource<CardModel> {

    private String cardNumber;
    private Long accountId;
    private String cardType;
    private Long totalLimit;
    private Long amountUsed;
    private Long availableAmount;
    @ToString.Exclude
    @JsonIgnore
    private OwnerModel owner;

    public CardModel(CardJpa entity) {
        super(entity);
        this.setCardNumber(entity.getCardNumber());
        this.setCardType(entity.getCardType());
        this.setTotalLimit(entity.getTotalLimit());
        this.setAmountUsed(entity.getAmountUsed());
        this.setAvailableAmount(entity.getAvailableAmount());
        if (entity.getAccount() != null) {
            this.setAccountId(entity.getAccount().getId());
            this.setOwner(new OwnerModel(entity.getAccount().getOwner()));
        }
    }

    @Override
    public CardJpa toEntityOnlyId() {
        return CardJpa.builder().id(getId()).build(); //NOSONAR (S3252)
    }

    public CardJpa toEntity() {
        return CardJpa.builder() //NOSONAR (S3252)
                .cardNumber(getCardNumber())
                .cardType(getCardType())
                .totalLimit(getTotalLimit())
                .amountUsed(getAmountUsed())
                .availableAmount(getAvailableAmount())
                .account(buildEntityWithIdOrNull(getAccountId()))
                .build();
    }

    @Override
    public CardModel fromEntity(CardJpa entity) {
        return new CardModel(entity);
    }
}
