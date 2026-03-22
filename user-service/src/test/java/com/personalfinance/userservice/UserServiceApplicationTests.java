package com.personalfinance.userservice;
import com.personalfinance.userservice.infrastructure.config.TestTokenBlocklistConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
@SpringBootTest
@Import(TestTokenBlocklistConfig.class)
class UserServiceApplicationTests {
    @Test
    void contextLoads() {}
}
