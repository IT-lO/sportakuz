package com.icio.sportakuz.config.security;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;
@Service
public class RecaptchaService {

    private static final String RECAPTCHA_SECRET = "6LerzT8sAAAAACS6YKf3aFcUd9d6pM-8LVjklgKs";
    private static final String GOOGLE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public boolean verify(String responseToken) {
        if (responseToken == null || responseToken.isEmpty()) {
            return false;
        }

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secret", RECAPTCHA_SECRET);
        map.add("response", responseToken);

        try {
            Map result = restTemplate.postForObject(GOOGLE_VERIFY_URL, map, Map.class);

            return result != null && (Boolean) result.get("success");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}