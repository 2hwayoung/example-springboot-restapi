package com.example.restapi.domain.member.member.service;

import com.example.restapi.domain.member.member.entity.Member;
import com.example.restapi.standard.util.Utils;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    public String genAccessToken(Member member) {
        int expireSeconds = 60 * 60 * 24 * 365;

        return Utils.Jwt.createToken(
                Keys.hmacShaKeyFor("abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890".getBytes()),
                expireSeconds,
                Map.of("id", member.getId(), "username", member.getUsername())
        );
    }

    public Map<String, Object> getPayload(SecretKey secretKey, String token) {
        Map<String, Object> payload = Utils.Jwt.getPayload(secretKey, token);

        if(payload == null) return null;

        Number idNo = (Number)payload.get("id");
        long id = idNo.longValue();

        String username = (String)payload.get("username");

        return Map.of("id", id, "username", username);
    }
}
