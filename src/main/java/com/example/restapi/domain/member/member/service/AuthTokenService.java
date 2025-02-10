package com.example.restapi.domain.member.member.service;

import com.example.restapi.domain.member.member.entity.Member;
import com.example.restapi.standard.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    @Value("${custom.jwt.secret-key}")
    private String keyString;
    @Value("${custom.jwt.expire-seconds}")
    private int expireSeconds;

    String genAccessToken(Member member) {

        return Utils.Jwt.createToken(
                keyString,
                expireSeconds,
                Map.of("id", member.getId(), "username", member.getUsername())
        );
    }

    Map<String, Object> getPayload(String token) {
        if(!Utils.Jwt.isTokenValid(keyString, token)) {
            return null;
        }
        Map<String, Object> payload = Utils.Jwt.getPayload(keyString, token);

        Number idNo = (Number)payload.get("id");
        long id = idNo.longValue();

        String username = (String)payload.get("username");

        return Map.of("id", id, "username", username);
    }
}
