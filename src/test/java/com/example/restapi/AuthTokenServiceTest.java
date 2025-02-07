package com.example.restapi;

import com.example.restapi.domain.member.member.entity.Member;
import com.example.restapi.domain.member.member.service.AuthTokenService;
import com.example.restapi.domain.member.member.service.MemberService;
import com.example.restapi.standard.util.Utils;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthTokenServiceTest {
    @Autowired
    private AuthTokenService authTokenService;
    @Autowired
    private MemberService memberService;

    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890".getBytes());

    @Test
    @DisplayName("AuthTokenService 생성")
    void init() {
        assertThat(authTokenService).isNotNull();
    }

    @Test
    @DisplayName("jwt 생성")
    void createToken() {
        int expireSeconds = 60 * 60 * 24 * 365;


        Map<String, Object> originPayload = Map.of("name", "john", "age", 23);
        String jwtStr = Utils.Jwt.createToken(SECRET_KEY, expireSeconds, originPayload);
        assertThat(jwtStr).isNotBlank();

        Jwt<?,?> parsedJwt = Jwts
                .parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parse(jwtStr);

        Map<String, Object> parsedPayload = (Map<String, Object>) parsedJwt.getPayload();
        assertThat(parsedPayload).containsAllEntriesOf(originPayload);
    }

    @Test
    @DisplayName("access token 생성")
    void accessToken() {
        // jwt -> access token jwt
        Member member = memberService.findByUsername("user1").get();
        String accessToken = authTokenService.genAccessToken(member);
        assertThat(accessToken).isNotBlank();
        System.out.println("accessToken = " + accessToken);
    }

    @Test
    @DisplayName("jwt valid check")
    void checkValidToken() {
        Member member = memberService.findByUsername("user1").get();
        String accessToken = authTokenService.genAccessToken(member);

        boolean isValidToken = Utils.Jwt.isTokenValid(SECRET_KEY, accessToken);
        assertThat(isValidToken).isTrue();
    }

}
