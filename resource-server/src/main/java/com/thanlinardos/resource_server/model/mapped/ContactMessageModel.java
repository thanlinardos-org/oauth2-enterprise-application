package com.thanlinardos.resource_server.model.mapped;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.thanlinardos.resource_server.model.entity.contact.ContactMessageJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class ContactMessageModel extends BasicIdModel<ContactMessageJpa, ContactMessageModel> implements Serializable {

    private String contactId;
    private String contactName;
    private String contactEmail;
    private String subject;
    private String message;

    @JsonCreator
    public ContactMessageModel(String contactId, String contactName, String contactEmail, String subject, String message) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.subject = subject;
        this.message = message;
    }

    public ContactMessageModel(ContactMessageJpa entity) {
        super(entity);
        this.setContactId(entity.getContactId());
        this.setContactName(entity.getContactName());
        this.setContactEmail(entity.getContactEmail());
        this.setSubject(entity.getSubject());
        this.setMessage(entity.getMessage());
    }

    @Override
    public ContactMessageJpa toEntityOnlyId() {
        return ContactMessageJpa.builder().id(getId()).build(); //NOSONAR (S3252)
    }

    public ContactMessageJpa toEntity() {
        return ContactMessageJpa.builder() //NOSONAR (S3252)
                .id(getId())
                .contactId(getContactId())
                .contactName(getContactName())
                .contactEmail(getContactEmail())
                .subject(getSubject())
                .message(getMessage())
                .build();
    }

    @Override
    public ContactMessageModel fromEntity(ContactMessageJpa entity) {
        return new ContactMessageModel(entity);
    }
}
