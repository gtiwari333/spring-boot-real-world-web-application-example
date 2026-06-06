package gt.app.frwk;

import com.codeborne.selenide.Browsers;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8088")
public abstract class BaseSeleniumTest extends AbstractIntegrationTest {

    @BeforeAll
    public static void init() {
        Configuration.headless = true;
        Configuration.browser = Browsers.CHROME;
        Configuration.browserCapabilities = new ChromeOptions();
        ((ChromeOptions) Configuration.browserCapabilities).addArguments("--no-sandbox");
    }

    @BeforeEach
    void beforeEach() {
        Configuration.baseUrl = "http://localhost:8088";
    }

    @AfterEach
    public void resetPage() {
        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();
    }
}
