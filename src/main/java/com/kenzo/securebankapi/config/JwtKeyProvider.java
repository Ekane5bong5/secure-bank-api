package com.kenzo.securebankapi.config;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtKeyProvider {

    public static final SecretKey JWT_KEY =
            Keys.secretKeyFor(SignatureAlgorithm.HS512);

}
