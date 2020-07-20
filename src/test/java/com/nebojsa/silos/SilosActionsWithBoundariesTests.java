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
public class SilosActionsWithBoundariesTests {

    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();
    SiloDto siloDtoFirst = createSiloDto("first");
    SiloDto siloDtoSecond = createSiloDto("second");
    HttpEntity<SiloDto> siloFirstEntity = new HttpEntity<>(siloDtoFirst, headers);
    HttpEntity<SiloDto> siloSecondEntity = new HttpEntity<>(siloDtoSecond, headers);

    @Test
    public void testActionWithBoundaries() {
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
        Long fillFirstTimestamp = responseActionFillFirst.getBody().getUpdatedInMillis();

        // check level for Fisrt silo
        LevelDto responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(100), responseLevelFirst.getLevel());

        // check level for all silos
        LevelAllDto responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
        assertEquals(Long.valueOf(100), responseLevelAll.getLevel());

        // add amount to First silo
        ResponseEntity<ActionDto> responseActionFillFirst2 = restTemplate.exchange(createURLWithPort("/action/do"), HttpMethod.POST, actionFillFirstEntity, ActionDto.class);
        // collect timestamp of this action
        Long fillFirstTimestamp2 = responseActionFillFirst2.getBody().getUpdatedInMillis();

        // check level for Fisrt silo
        responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(200), responseLevelFirst.getLevel());

        // check level for all silos
        responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
        assertEquals(Long.valueOf(200), responseLevelAll.getLevel());


        // create move action fom First to Second silo at point in time before there was 200 in silo
        ActionDto actionDtoMove = createActionDto(siloDtoSecond.getId(), siloDtoFirst.getId(), ActionName.move, 200L, fillFirstTimestamp2 - 1);
        HttpEntity<ActionDto> actionMoveEntity = new HttpEntity<>(actionDtoMove, headers);

        // move from First to Second amount of 200
        ResponseEntity<ActionDto> responseActionMove = restTemplate.exchange(createURLWithPort("/action/do"), HttpMethod.POST, actionMoveEntity, ActionDto.class);
        // checking that Move 200 is not possible because at that point in time there was not enough level for this action
        assertTrue("Action status code is not 400", responseActionMove.getStatusCodeValue() == 400);

        // create move action with correct timestamp
        actionDtoMove = createActionDto(siloDtoSecond.getId(), siloDtoFirst.getId(), ActionName.move, 200L, fillFirstTimestamp2 + 1);
        actionMoveEntity = new HttpEntity<>(actionDtoMove, headers);

        // move from First to Second with corrected timestamp
        ResponseEntity<ActionDto> responseActionMove2 = restTemplate.exchange(createURLWithPort("/action/do"), HttpMethod.POST, actionMoveEntity, ActionDto.class);
        //  check move action is  succesfull
        assertTrue("Action status code is not 201", responseActionMove2.getStatusCodeValue() == 201);

        // check level for Fisrt silo
        responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(0), responseLevelFirst.getLevel());

        // check level for Second silo
        LevelDto responseLevelSecond = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoSecond.getId()), LevelDto.class);
        assertEquals(Long.valueOf(200), responseLevelSecond.getLevel());

        // check level for all silos
        responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
        assertEquals(Long.valueOf(200), responseLevelAll.getLevel());

        // create fill action for second silo with amount 801
        ActionDto actionDtoFillSecond = createActionDto(siloDtoSecond.getId(), null, ActionName.fill, 801L, System.currentTimeMillis());
        HttpEntity<ActionDto> actionFillSecondEntity = new HttpEntity<>(actionDtoFillSecond, headers);

        // POST fill action to Second silo with amount 801
        ResponseEntity<ActionDto> responseActionFillSecond = restTemplate.exchange(
                createURLWithPort("/action/do"), HttpMethod.POST, actionFillSecondEntity, ActionDto.class);

        // check that it is not possible to add over the capacity of silos
        assertTrue("Action status code is not 400", responseActionFillSecond.getStatusCodeValue() == 400);


        // check level for Fisrt silo
        responseLevelFirst = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoFirst.getId()), LevelDto.class);
        assertEquals(Long.valueOf(0), responseLevelFirst.getLevel());

        // check level for Second silo
        responseLevelSecond = restTemplate.getForObject(createURLWithPort("/silo/level/" + siloDtoSecond.getId()), LevelDto.class);
        assertEquals(Long.valueOf(200), responseLevelSecond.getLevel());

        // check level for all silos
        responseLevelAll = restTemplate.getForObject(createURLWithPort("/silo/level/all"), LevelAllDto.class);
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
