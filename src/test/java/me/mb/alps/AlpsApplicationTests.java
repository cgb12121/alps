package me.mb.alps;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class AlpsApplicationTests {

    @Test
    void contextLoads() {
    }

}
