package com.abdr.employee.security.captcha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class CaptchaService {
    @Value("${google.recaptcha.key.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.verify-url}")
    private String recaptchaVerifyUrl;

    private final RestTemplate restTemplate;

    public CaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateCaptcha(String captchaResponse) {
        if (!StringUtils.hasText(captchaResponse)) {
            return false;
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", captchaResponse);

        RecaptchaResponse response = restTemplate.postForObject(
                recaptchaVerifyUrl,
                params,
                RecaptchaResponse.class
        );

        return response != null && response.isSuccess() && response.getScore() >= 0.5;
    }
}