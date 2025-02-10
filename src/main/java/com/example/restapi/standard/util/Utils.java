package com.example.restapi.standard.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class Utils {
    public static class Json {
        private static final ObjectMapper mapper = new ObjectMapper();

        public static String toString(Object obj) {
            try {
                return mapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static class Jwt {
        public static String createToken(Key secretKey, int expireSeconds, Map<String, Object> claims) {
            Date issuedAt = new Date();
            Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);
            return Jwts.builder()
                    .claims(claims)
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(secretKey) // SignatureAlgorithm.HS256
                    .compact();
        }

        public static boolean isTokenValid(SecretKey secretKey, String token) {
            try {
                Jwts
                        .parser()
                        .verifyWith(secretKey)
                        .build()
                        .parse(token);
            }catch (Exception e) {
                return false;
            }
            return true;
        }

        public static Map<String, Object> getPayload(SecretKey secretKey, String jwtStr) {
            return (Map<String, Object>) Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                    .getPayload();
        }
    }
}
