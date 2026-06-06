package gt.report;

import gt.report.frwk.TestContainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
@Import(TestContainerConfig.class)
class ReportServiceAppTest {

    @Test
    void contextLoads() {
        assertTrue(true, "Context loads !!!");
    }
}
