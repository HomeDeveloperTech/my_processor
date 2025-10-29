package com.fiserv.fico;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Disabled to avoid full context load in unit test suite")
@SpringBootTest
class FicoProcessorApplicationTests {

  @Test
  void contextLoads() {}
}
