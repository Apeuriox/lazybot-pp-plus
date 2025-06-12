package me.aloic.lazybotppplus.controller;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.WebResult;
import me.aloic.lazybotppplus.service.AuthService;
import me.aloic.lazybotppplus.util.JwtTokenUtil;
import me.aloic.lazybotppplus.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/token")
    public WebResult getToken(@RequestParam Integer clientId, @RequestParam String clientSecret) {
        logger.info("Received token request from client: {}", clientId);
        if (authService.validate(clientId, clientSecret)) {
            String token = jwtTokenUtil.tokenGenerate(Map.of("id", clientId));
            return ResultUtil.success(token, "Token issued");
        } else {
            return ResultUtil.error(401, "Invalid credentials");
        }
    }

//    @PostMapping("/addClient")
//    public WebResult addClient(@RequestParam Integer clientId, @RequestParam String secret, @RequestParam String desc) {
//        logger.info("Creating client with id: {}", clientId);
//        ApiClientPO client = authService.createClient(clientId, secret, desc);
//        return ResultUtil.success(Map.of(
//                "clientId", client.getClientId(),
//                "secret", secret
//        ), "client created");
//    }
}