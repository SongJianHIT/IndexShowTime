/**
 * @projectName stock_parent
 * @package tech.songjian.stock
 * @className tech.songjian.stock.TestJWT
 */
package tech.songjian.stock;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

/**
 * TestJWT
 * @description
 * @author SongJian
 * @date 2023/2/15 19:56
 * @version
 */
public class TestJWT {

    @Test
    public void testCreatToken() {
        // 声明私钥
        // 私钥是颁发 token 凭证方私有，不能泄漏！
        String secu = "SongJian";
        // 基于构建者模式，生成Token
        String token = Jwts.builder()
                .setId(UUID.randomUUID().toString()) // 设置唯一标识，一般是用户id
                .setSubject("JRZS") // 设置token主题
                .claim("name", "zhangsan")  // 通过claim自定义数据，保证是 key-value
                .claim("age", "18")
                .setIssuedAt(new Date()) // 设置票据颁发时间
                .setExpiration(new Date(2023, 2, 15, 20, 15)) // 设置票据失效时间
                .signWith(SignatureAlgorithm.HS256, secu) // 设置加密算法 和 私钥
                .compact();
        System.out.println(token);
    }

    @Test
    public void testVerify(){
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2ZmM5NWJiNy0xYWRiLTQxMGMtYWJkYy0xZWRlMjgwYzc3ZmEiLCJzdWIiOiJKUlpTIiwibmFtZSI6InpoYW5nc2FuIiwiYWdlIjoiMTgiLCJpYXQiOjE2NzY0NjMwNjcsImV4cCI6NjE2MzcwMjY1MDB9.ThzgUsNM7Jd_lJZeEYTs4X0KUB8FueV7qbmGvL7VVMk";
        Claims claims = Jwts.parser().setSigningKey("SongJian").parseClaimsJws(jwt).getBody();
        System.out.println(claims);
    }
}

