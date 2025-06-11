package me.aloic.lazybotppplus.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.aloic.lazybotppplus.component.SecretEncryption;
import me.aloic.lazybotppplus.entity.mapper.ApiClientMapper;
import me.aloic.lazybotppplus.entity.po.ApiClientPO;
import me.aloic.lazybotppplus.service.AuthService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService
{
    @Resource
    private ApiClientMapper apiClientMapper;

    @Resource
    private SecretEncryption secretEncryption;

    @Override
    public boolean validate(Integer clientId, String clientSecretRaw) {
        log.info("Validating client with id: {}", clientId);
        ApiClientPO client = apiClientMapper.selectByClientId(clientId);
        return client != null && client.getClientSecret().equals(secretEncryption.encryption(clientSecretRaw));
    }

    @Override
    public ApiClientPO createClient(Integer clientId, String rawSecret, String description) {
        log.info("Creating client with id: {}, description: {}", clientId, description);
        String encodedSecret = secretEncryption.encryption(rawSecret);
        ApiClientPO client = new ApiClientPO(clientId, encodedSecret, description);
        apiClientMapper.insert(client);
        return client;
    }
}
