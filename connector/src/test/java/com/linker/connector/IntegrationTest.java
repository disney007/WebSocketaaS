package com.linker.connector;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Import(TestConfig.class)
@Ignore
@Slf4j
public class IntegrationTest {
}
