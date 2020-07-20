package com.nebojsa.silos;

import com.nebojsa.silos.constants.ActionName;
import com.nebojsa.silos.dto.ActionDto;
import com.nebojsa.silos.dto.LevelAllDto;
import com.nebojsa.silos.dto.LevelDto;
import com.nebojsa.silos.dto.ListActionDto;
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
public class SilosActionsWithUpdatesTests {

    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();
    SiloDto siloDtoFirst = createSiloDto("first");
    SiloDto siloDtoSecond = createSiloDto("second");
    HttpEntity<SiloDto> siloFirstEntity = new HttpEntity<>(siloDtoFirst, headers);
    HttpEntity<SiloDto> siloSecondEntity = new HttpEntity<>(siloDtoSecond, headers);

    @Test
    public void testActionWithUpdateAndHistory() {
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

        // create fill action for first silo
        ActionDto actionDtoFillFirst = createActionDto(siloDtoFirst.getId(), null, ActionName.fill, 100L, System.currentTimeMillis());
        // fill action entities
        HttpEntity<ActionDto> actionFillFirstEntity = new HttpEntity<>(actionDtoFillFirst, headers);
        // POST fill action to FIRST silo
        ResponseEntity<ActionDto> responseActionFillFirst = restTemplate.exchange(
                createURLWithPort("/action/do"), HttpMethod.POST, actionFillFirstEntity, ActionDto.class);
        // check fill action succesfull
        assertTrue("Action status code is not 201", responseActionFillFirst.getStatusCodeValue() == 201);

        // collect timestamp for later
        Long timestamp1 = responseActionFillFirst.getBody().getUpdatedInMillis();
        Long actionId1 = responseActionFillFirst.getBody().getId();

        // check level for Fisrt silo
        LevelDto responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(100), responseLevelFirst.getLevel());

        // check level for all silos
        LevelAllDto responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
        assertEquals(Long.valueOf(100), responseLevelAll.getLevel());

        // add amount to First silo
        ResponseEntity<ActionDto> responseActionFillFirst2 = restTemplate.exchange(createURLWithPort("/action/do"), HttpMethod.POST, actionFillFirstEntity, ActionDto.class);
        // collect timestamp of this action
        Long timestamp2 = responseActionFillFirst2.getBody().getUpdatedInMillis();


        // check level for Fisrt silo
        responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(200), responseLevelFirst.getLevel());

        // check level for all silos
        responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
        assertEquals(Long.valueOf(200), responseLevelAll.getLevel());


        // update action to add to second and not to First and ammount is 99
        ActionDto actionDtoUpdate = createActionDto(siloDtoSecond.getId(), null, ActionName.fill, 99L, timestamp1);
        // set ID of action for update
        actionDtoUpdate.setId(actionId1);
        HttpEntity<ActionDto> actionUpdateEntity = new HttpEntity<>(actionDtoUpdate, headers);

        // update endpoint
        ResponseEntity<ActionDto> responseActionUpdate = restTemplate.exchange(createURLWithPort("/action/update"), HttpMethod.PUT, actionUpdateEntity, ActionDto.class);
        // checking that update is succesfull
        assertTrue("Action status code is not 200", responseActionUpdate.getStatusCodeValue() == 200);

        // check level for First silo
        responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(100), responseLevelFirst.getLevel());

        // check level for Second silo
        LevelDto responseLevelSecond = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoSecond.getId()), LevelDto.class);
        assertEquals(Long.valueOf(99), responseLevelSecond.getLevel());

        // check level for all silos
        responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
        assertEquals(Long.valueOf(199), responseLevelAll.getLevel());


        // find all versions of action
        ListActionDto responseActionVersion = restTemplate.getForObject(createURLWithPort("/action/versions/" + actionId1), ListActionDto.class);
        // check that previous in version of action amount is 100
        assertEquals(responseActionVersion.getActions().get(0).getAmount(), Long.valueOf(100));
        // check that current version of action amount is 99
        assertEquals(responseActionVersion.getActions().get(1).getAmount(), Long.valueOf(99));


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
