package com.thanlinardos.resource_server.model.mapped;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.thanlinardos.resource_server.model.entity.contact.NoticeDetailsJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class NoticeDetailsModel extends BasicIdModel<NoticeDetailsJpa, NoticeDetailsModel> implements Serializable {

    private String noticeSummary;
    private String noticeDetails;
    private LocalDate noticBegDt;
    private LocalDate noticEndDt;

    @JsonCreator
    public NoticeDetailsModel(String noticeSummary, String noticeDetails, LocalDate noticBegDt, LocalDate noticEndDt) {
        this.noticeSummary = noticeSummary;
        this.noticeDetails = noticeDetails;
        this.noticBegDt = noticBegDt;
        this.noticEndDt = noticEndDt;
    }

    public NoticeDetailsModel(NoticeDetailsJpa entity) {
        super(entity);
        this.setNoticeSummary(entity.getNoticeSummary());
        this.setNoticeDetails(entity.getNoticeDetails());
        this.setNoticBegDt(entity.getNoticBegDt());
        this.setNoticEndDt(entity.getNoticEndDt());
    }

    @Override
    public NoticeDetailsJpa toEntity() {
        return NoticeDetailsJpa.builder() //NOSONAR (S3252)
                .id(getId())
                .noticeSummary(getNoticeSummary())
                .noticeDetails(getNoticeDetails())
                .noticBegDt(getNoticBegDt())
                .noticEndDt(getNoticEndDt())
                .build();
    }

    @Override
    public NoticeDetailsJpa toEntityOnlyId() {
        return NoticeDetailsJpa.builder().id(getId()).build(); //NOSONAR (S3252)
    }

    @Override
    public NoticeDetailsModel fromEntity(NoticeDetailsJpa entity) {
        return new NoticeDetailsModel(entity);
    }
}
