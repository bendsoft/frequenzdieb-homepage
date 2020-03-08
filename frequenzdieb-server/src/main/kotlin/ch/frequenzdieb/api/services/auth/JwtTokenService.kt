package ch.frequenzdieb.api.services.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.HashMap

@Component
class JwtTokenService {
    @Value("\${frequenzdieb.security.jwt.secret}")
    lateinit var secret: String

    @Value("\${frequenzdieb.security.jwt.expiration}")
    lateinit var expiration: Number

    fun generateToken(account: Account): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["role"] = account.role

        val createdDate = Date()

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(account.username)
            .setIssuedAt(createdDate)
            .setExpiration(calculateExpirationDate(createdDate))
            .signWith(SignatureAlgorithm.HS512, secret.toByteArray())
            .compact()
    }

    private fun calculateExpirationDate(createdDate: Date) =
        Date(createdDate.time + expiration.toLong() * 10000)

    fun getAllClaimsFromToken(token: String): Claims =
        Jwts.parser()
            .setSigningKey(secret.toByteArray())
            .parseClaimsJws(token)
            .body

    fun isTokenValid(token: String) = getAllClaimsFromToken(token).expiration.before(Date())
}
