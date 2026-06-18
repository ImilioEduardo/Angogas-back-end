package ao.angogas.backend;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class BackendApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
	}

}
