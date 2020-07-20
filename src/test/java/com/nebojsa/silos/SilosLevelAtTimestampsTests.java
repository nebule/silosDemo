package com.nebojsa.silos;

import com.nebojsa.silos.constants.ActionName;
import com.nebojsa.silos.dto.ActionDto;
import com.nebojsa.silos.dto.LevelAllDto;
import com.nebojsa.silos.dto.LevelDto;
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
public class SilosLevelAtTimestampsTests {

    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();
    SiloDto siloDtoFirst = createSiloDto("first");
    SiloDto siloDtoSecond = createSiloDto("second");
    HttpEntity<SiloDto> siloFirstEntity = new HttpEntity<>(siloDtoFirst, headers);
    HttpEntity<SiloDto> siloSecondEntity = new HttpEntity<>(siloDtoSecond, headers);

    @Test
    public void testLevelAtTimestamps() {
        //create silos
        ResponseEntity<SiloDto> responseSiloFirst = restTemplate.exchange(
                createURLWithPort("/silo/create"), HttpMethod.POST, siloFirstEntity, SiloDto.class);
        ResponseEntity<SiloDto> responseSiloSecond = restTemplate.exchange(
                createURLWithPort("/silo/create"), HttpMethod.POST, siloSecondEntity, SiloDto.class);

        // set ids from reponse
        siloDtoFirst.setId(responseSiloFirst.getBody().getId());
        siloDtoSecond.setId(responseSiloSecond.getBody().getId());

        assertTrue(responseSiloFirst.getStatusCodeValue() == 201);
        assertTrue(responseSiloSecond.getStatusCodeValue() == 201);

        // create fill action for first and second silo
        ActionDto actionDtoFillFirst = createActionDto(siloDtoFirst.getId(), null, ActionName.fill, 100L, null);
        ActionDto actionDtoFillSecond = createActionDto(siloDtoSecond.getId(), null, ActionName.fill, 50L, null);
        // fill action entities
        HttpEntity<ActionDto> actionFillFirstEntity = new HttpEntity<>(actionDtoFillFirst, headers);
        HttpEntity<ActionDto> actionFillSecondEntity = new HttpEntity<>(actionDtoFillSecond, headers);

        //  add amount to First silo
        ResponseEntity<ActionDto> responseActionFillFirst = restTemplate.exchange(
                createURLWithPort("/action/do"), HttpMethod.POST, actionFillFirstEntity, ActionDto.class);
        // collect timestamp for later
        Long timestamp1 = responseActionFillFirst.getBody().getUpdatedInMillis();

        // check level for First silo
        LevelDto responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(100), responseLevelFirst.getLevel());

        // check level for Second silo
        LevelDto responseLevelSecond = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoSecond.getId()), LevelDto.class);
        assertEquals(Long.valueOf(0), responseLevelSecond.getLevel());

        // check level for all silos
        LevelAllDto responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
        assertEquals(Long.valueOf(100), responseLevelAll.getLevel());

        // add amount to First silo
        responseActionFillFirst = restTemplate.exchange(createURLWithPort("/action/do"), HttpMethod.POST, actionFillFirstEntity, ActionDto.class);
        // collect timestamp of this action
        Long timestamp2 = responseActionFillFirst.getBody().getUpdatedInMillis();

        // check level for First silo
        responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(200), responseLevelFirst.getLevel());

        // check level for Second silo
        responseLevelSecond = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoSecond.getId()), LevelDto.class);
        assertEquals(Long.valueOf(0), responseLevelSecond.getLevel());

        // check level for all silos
        responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
        assertEquals(Long.valueOf(200), responseLevelAll.getLevel());


        //  add amount to Second silo
        ResponseEntity<ActionDto> responseActionFillSecond = restTemplate.exchange(
                createURLWithPort("/action/do"), HttpMethod.POST, actionFillSecondEntity, ActionDto.class);

        // check level for First silo
        responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(200), responseLevelFirst.getLevel());

        // check level for Second silo
        responseLevelSecond = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoSecond.getId()), LevelDto.class);
        assertEquals(Long.valueOf(50), responseLevelSecond.getLevel());

        // check level for all silos
        responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
        assertEquals(Long.valueOf(250), responseLevelAll.getLevel());


        // check level for First silo at TIMESTAMP1
        responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId() + "/" + timestamp1), LevelDto.class);
        assertEquals(Long.valueOf(100), responseLevelFirst.getLevel());

        // check level for Second silo  at TIMESTAMP1
        responseLevelSecond = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoSecond.getId() + "/" + timestamp1), LevelDto.class);
        assertEquals(Long.valueOf(0), responseLevelSecond.getLevel());

        // check level for all silos  at TIMESTAMP1
        responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all/" + timestamp1), LevelAllDto.class);
        assertEquals(Long.valueOf(100), responseLevelAll.getLevel());


        // check level for First silo at TIMESTAMP2
        responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId() + "/" + timestamp2), LevelDto.class);
        assertEquals(Long.valueOf(200), responseLevelFirst.getLevel());

        // check level for Second silo  at TIMESTAMP2
        responseLevelSecond = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoSecond.getId() + "/" + timestamp2), LevelDto.class);
        assertEquals(Long.valueOf(0), responseLevelSecond.getLevel());

        // check level for all silos  at TIMESTAMP2
        responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all/" + timestamp2), LevelAllDto.class);
        assertEquals(Long.valueOf(200), responseLevelAll.getLevel());

    }

    private ActionDto createActionDto(Long siloToId, Long siloFromId, ActionName actionName, Long amount, Long timestamp) {
        ActionDto actionDto = new ActionDto();
        actionDto.setSiloToId(siloToId);
        actionDto.setSiloFromId(siloFromId);
        actionDto.setActionName(actionName);
        actionDto.setAmount(amount);
        actionDto.setUpdatedInMillis(timestamp);
        return actionDto;
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
