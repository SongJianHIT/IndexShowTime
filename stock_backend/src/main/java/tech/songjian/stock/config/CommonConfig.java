package tech.songjian.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.songjian.stock.utils.IdWorker;
import tech.songjian.stock.utils.ParserStockInfoUtil;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description 定义公共配置类
 */
@Configuration
public class CommonConfig {

    /**
     * 定义密码加密和解密器 bean
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * id 生成器 bean
     * @return
     */
    @Bean
    public IdWorker idWorker() {
        return new IdWorker();
    }

    @Bean
    public ParserStockInfoUtil parserStockInfoUtil(IdWorker idWorker) {
        return new ParserStockInfoUtil(idWorker);
    }
}
