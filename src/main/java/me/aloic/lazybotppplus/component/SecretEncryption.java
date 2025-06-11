package me.aloic.lazybotppplus.component;

import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SecretEncryption {

    @Value("${lazybot.decryption_key}")
    private String key;

    private SymmetricCrypto des;

    @PostConstruct
    public void init() {
        this.des = new SymmetricCrypto(SymmetricAlgorithm.DES, key.getBytes(StandardCharsets.UTF_8));
    }

    public String decrypt(String content) {
        return des.decryptStr(content);
    }

    public String encryption(String content) {
        return des.encryptHex(content);
    }
}
