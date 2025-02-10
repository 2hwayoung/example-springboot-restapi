package com.example.restapi.domain.member.member.service;

import com.example.restapi.domain.member.member.entity.Member;
import com.example.restapi.standard.util.Utils;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    @Value("${custom.jwt.secret-key}")
    private String keyString;
    @Value("${custom.jwt.expire-seconds}")
    private int expireSeconds;

    public String genAccessToken(Member member) {

        return Utils.Jwt.createToken(
                keyString,
                expireSeconds,
                Map.of("id", member.getId(), "username", member.getUsername())
        );
    }

    public Map<String, Object> getPayload(String token) {
        Map<String, Object> payload = Utils.Jwt.getPayload(keyString, token);
        if(payload == null) return null;

        Number idNo = (Number)payload.get("id");
        long id = idNo.longValue();

        String username = (String)payload.get("username");

        return Map.of("id", id, "username", username);
    }
}
