package com.thanlinardos.resource_server.controller.rest;

import com.thanlinardos.resource_server.model.info.ContactMessage;
import com.thanlinardos.resource_server.service.contact.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Random;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageService contactMessageService;
    private final Random random = new SecureRandom();

    @PostMapping("/contact")
    public ResponseEntity<ContactMessage> saveContactInquiryDetails(@RequestBody ContactMessage contact) {
        contact.setContactId(getServiceReqNumber(random));
        contact.setCreateDt(LocalDate.now());
        contactMessageService.saveContactMessage(contact);
        return ResponseEntity.ok(contact);
    }

    public static String getServiceReqNumber(Random random) {
        int ranNum = random.nextInt(999999999 - 9999) + 9999;
        return "SR" + ranNum;
    }
}
