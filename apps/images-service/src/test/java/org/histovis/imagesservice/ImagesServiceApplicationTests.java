package org.histovis.imagesservice;

import org.histovis.imagesservice.config.TestContainersConfig;
import org.histovis.commons.storage.ObjectStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class ImagesServiceApplicationTests {

    @MockBean
    private ObjectStorageService objectStorageService;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }
}
