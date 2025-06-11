package me.aloic.lazybotppplus.service;

import me.aloic.lazybotppplus.entity.po.ApiClientPO;

public interface AuthService
{
    boolean validate(Integer clientId, String clientSecretRaw);

    ApiClientPO createClient(Integer clientId, String rawSecret, String description);
}
