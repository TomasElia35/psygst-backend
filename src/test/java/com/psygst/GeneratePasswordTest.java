package com.psygst;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordTest {
    @Test
    public void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("====== HASH PARA: admin123 ======");
        System.out.println(encoder.encode("admin123"));
        System.out.println("=================================");
    }
}
