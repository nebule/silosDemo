package com.nebojsa.silos;

import com.nebojsa.silos.dto.SiloDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SilosTests {

    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    @Test
    public void testCreateAndRetrieveSilo() {
        SiloDto siloDto = createSiloDto("first");
        HttpEntity<SiloDto> entity = new HttpEntity<>(siloDto, headers);

        //create silo
        ResponseEntity<SiloDto> response = restTemplate.exchange(
                createURLWithPort("/silo/create"), HttpMethod.POST, entity, SiloDto.class);

        String actual = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        siloDto.setId(response.getBody().getId());

        assertTrue("Silo response does not have proper location header ", actual.contains("/silo/get/"));

        SiloDto getResponse = restTemplate.getForObject(createURLWithPort("/silo/get/" + siloDto.getId()), SiloDto.class);

        assertEquals("SiloDto is not as expected", siloDto, getResponse);
    }


    private SiloDto createSiloDto(String name) {
        SiloDto siloDto = new SiloDto();
        siloDto.setName(name);
        siloDto.setCapacity(1000L);
        return siloDto;
    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}
