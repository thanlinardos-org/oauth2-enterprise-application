package com.thanlinardos.resource_server.environment.certs;

import com.thanlinardos.spring_enterprise_library.spring_cloud_security.environment.certs.service.UpdateCertificatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@RestController("/certs")
@RequiredArgsConstructor
public class UpdateCertificatesController {

    private final UpdateCertificatesService updateCertificatesService;

    @PostMapping("/update-server")
    public void updateServerCertificate(MultipartFile pem) throws IOException, CertificateException, NoSuchAlgorithmException {
        updateCertificatesService.saveServerCertificate(pem);
    }

    @PostMapping("/update-client")
    public void updateClientCertificate(MultipartFile pem) throws IOException, CertificateException, NoSuchAlgorithmException {
        updateCertificatesService.saveClientCertificate(pem);
    }
}
