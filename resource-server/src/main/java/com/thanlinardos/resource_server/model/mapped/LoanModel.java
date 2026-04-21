package com.thanlinardos.resource_server.model.mapped;

import com.thanlinardos.resource_server.model.entity.economy.LoanJpa;
import com.thanlinardos.resource_server.model.mapped.base.BasicOwnedIdModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class LoanModel extends BasicOwnedIdModel<LoanJpa> implements Serializable {

    private LocalDate startDt;
    private String loanType;
    private Long totalLoan;
    private Long amountPaid;
    private Long outstandingAmount;

    public LoanModel() {
        super();
    }

    public LoanModel(LoanJpa entity) {
        super(entity);
        this.setStartDt(entity.getStartDt());
        this.setLoanType(entity.getLoanType());
        this.setTotalLoan(entity.getTotalLoan());
        this.setAmountPaid(entity.getAmountPaid());
        this.setOutstandingAmount(entity.getOutstandingAmount());
        this.setOwner(new OwnerModel(entity.getOwner()));
    }

    @Override
    public LoanJpa toEntityOnlyId() {
        return LoanJpa.builder().id(getId()).build(); //NOSONAR (S3252)
    }

    public LoanJpa toEntity() {
        return LoanJpa.builder() //NOSONAR (S3252)
                .id(getId())
                .startDt(getStartDt())
                .loanType(getLoanType())
                .totalLoan(getTotalLoan())
                .amountPaid(getAmountPaid())
                .outstandingAmount(getOutstandingAmount())
                .owner(getOwner() != null ? getOwner().toEntity() : null)
                .build();
    }

    @Override
    public BasicOwnedIdModel<LoanJpa> fromEntity(LoanJpa entity) {
        return new LoanModel(entity);
    }
}
