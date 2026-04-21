package com.thanlinardos.resource_server.service.contact;

import com.thanlinardos.resource_server.model.entity.contact.ContactMessageJpa;
import com.thanlinardos.resource_server.model.info.Contact;
import com.thanlinardos.resource_server.repository.api.ContactMessageRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.ApplicationScope;

import java.security.SecureRandom;
import java.util.Random;

import static com.thanlinardos.resource_server.controller.rest.ContactController.getServiceReqNumber;

@Slf4j
@Service
// @RequestScope
// @SessionScope
@ApplicationScope
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final Random random = new SecureRandom();

    @Getter
    @Setter
    private int counter = 0;

    /**
     * Save Contact Details into DB
     *
     * @param contact Contact
     */
    @Transactional
    public void saveMessageDetails(Contact contact) {
        ContactMessageJpa contactMessageJpa = fromContact(contact);
        contactMessageRepository.save(contactMessageJpa);
    }

    private ContactMessageJpa fromContact(Contact contact) {
        return ContactMessageJpa.builder() //NOSONAR (S3252)
                .contactEmail(contact.getEmail())
                .message(contact.getMessage())
                .contactName(contact.getName())
                .subject(contact.getSubject())
                .contactId(getServiceReqNumber(random))
                .build();
    }
}