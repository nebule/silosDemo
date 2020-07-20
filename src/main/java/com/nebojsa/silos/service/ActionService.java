package com.nebojsa.silos.service;

import com.nebojsa.silos.constants.ActionName;
import com.nebojsa.silos.dto.ActionDto;
import com.nebojsa.silos.dto.ActionMapper;
import com.nebojsa.silos.dto.LevelAllDto;
import com.nebojsa.silos.dto.LevelDto;
import com.nebojsa.silos.dto.ListActionDto;
import com.nebojsa.silos.entity.Action;
import com.nebojsa.silos.entity.Silo;
import com.nebojsa.silos.exceptions.SiloCapacityFullException;
import com.nebojsa.silos.exceptions.SiloNotEnoughLevelException;
import com.nebojsa.silos.repository.ActionRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Log
public class ActionService {

    private final ActionRepository actionRepository;

    private final SiloService siloService;

    private final ActionMapper actionMapper;

    @Autowired
    public ActionService(ActionRepository actionRepository, SiloService siloService, ActionMapper actionMapper) {
        this.actionRepository = actionRepository;
        this.siloService = siloService;
        this.actionMapper = actionMapper;
    }

    @Transactional
    public ActionDto doAction(ActionDto actionDto) {
        Action action = createActionEntity(actionDto);
        actionRepository.save(action);
        ActionDto responseActionDto = actionMapper.toDto(action);
        log.info("saving action: " + responseActionDto.toString());
        return responseActionDto;
    }

    @Transactional
    public ActionDto update(ActionDto actionDto) {
        Action action = actionRepository.findById(actionDto.getId()).orElseThrow(() -> new IllegalArgumentException("Action for id=" + actionDto.getId() + " Not found"));
        action.setUpdatedInMillis(actionDto.getUpdatedInMillis());

        mapAndVerifyAction(actionDto, action, actionDto.getUpdatedInMillis());

        ActionDto responseActionDto = actionMapper.toDto(actionRepository.save(action));
        log.info("updating action: " + responseActionDto.toString());

        return responseActionDto;
    }

    private Action createActionEntity(ActionDto actionDto) throws SiloCapacityFullException {
        Action action = new Action();
        if (actionDto.getUpdatedInMillis() != null) {
            action.setUpdatedInMillis(actionDto.getUpdatedInMillis());
        } else {
            action.setUpdatedInMillis(System.currentTimeMillis());
        }
        mapAndVerifyAction(actionDto, action, action.getUpdatedInMillis());

        return action;
    }

    private void mapAndVerifyAction(ActionDto actionDto, Action action, Long updatedInMillis) {
        Silo siloTo = null;
        Silo siloFrom = null;
        if (actionDto.getActionName().equals(ActionName.fill)
                || actionDto.getActionName().equals(ActionName.move)) {
            siloTo = siloService.findById(actionDto.getSiloToId());
            verifyCapacity(siloTo.getCapacity(), calculateLevel(siloTo.getId(), updatedInMillis).getLevel(), actionDto.getAmount());
        }
        if (actionDto.getActionName().equals(ActionName.move)
                || actionDto.getActionName().equals(ActionName.remove)
                || actionDto.getActionName().equals(ActionName.withdraw)) {
            siloFrom = siloService.findById(actionDto.getSiloFromId());
            verifyLevel(calculateLevel(siloFrom.getId(), updatedInMillis).getLevel(), actionDto.getAmount());
        }
        action.setFrom(siloFrom);
        action.setTo(siloTo);
        action.setActionName(actionDto.getActionName());
        action.setAmount(actionDto.getAmount());
    }


    private void verifyLevel(Long currentLevel, Long amount) throws SiloNotEnoughLevelException {
        log.info("Checking level current=" + currentLevel + ", amount=" + amount);
        if (currentLevel < amount) {
            log.warning("Not enough level to withdraw from silo current=" + currentLevel + ", amount=" + amount);
            throw new SiloNotEnoughLevelException("Not enough level to withdraw from silo");
        }
    }

    private void verifyCapacity(Long capacity, Long currentLevel, Long amount) throws SiloCapacityFullException {
        log.info("Checking capacity current=" + currentLevel + ", amount=" + amount);
        if (capacity < (currentLevel + amount)) {
            log.warning("Not enough capacity (" + capacity + ") to add amount into silo current=" + currentLevel + ", amount=" + amount);
            throw new SiloCapacityFullException("Not enough capacity to add amount into silo");
        }
    }

    public LevelAllDto calculateCurrentLevelAll() {
        Long currentTimestamp = System.currentTimeMillis();
        Long level = siloService.findAll().stream().mapToLong(silo -> calculateLevel(silo.getId(), currentTimestamp).getLevel()).sum();
        return createLevelAllDto(currentTimestamp, level);
    }

    public LevelAllDto calculateLevelAllAtTimestamp(Long timestamp) {
        Long level = siloService.findAll().stream().mapToLong(silo -> calculateLevel(silo.getId(), timestamp).getLevel()).sum();
        return createLevelAllDto(timestamp, level);
    }

    public LevelDto calculateCurrentLevel(Long siloId) {
        Long currentTimestamp = System.currentTimeMillis();
        return calculateLevel(siloId, currentTimestamp);
    }

    public LevelDto calculateLevel(Long siloId, Long timestamp) {
        log.info("calculating level at timestamp=" + timestamp + " for siloId=" + siloId);
        List<Action> addedActions = actionRepository.findAllByTo_IdAndUpdatedInMillisIsLessThanEqualOrderByUpdatedInMillisAsc(siloId, timestamp);
        List<Action> subtractedActions = actionRepository.findAllByFrom_IdAndUpdatedInMillisIsLessThanEqualOrderByUpdatedInMillisAsc(siloId, timestamp);
        long addedSum = addedActions.stream().mapToLong(Action::getAmount).sum();
        long subtractedSum = subtractedActions.stream().mapToLong(Action::getAmount).sum();
        long level = addedSum - subtractedSum;
        log.info("calculated level=" + level);
        return createLevelDto(siloId, timestamp, level);
    }

    public ActionDto getById(Long id) {
        Action action = actionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Action for id=" + id + " Not found"));
        return actionMapper.toDto(action);
    }

    private LevelDto createLevelDto(Long siloId, Long timestamp, Long level) {
        LevelDto levelDto = new LevelDto();
        levelDto.setLevel(level);
        levelDto.setSiloId(siloId);
        levelDto.setMeasureTimestamp(timestamp);
        return levelDto;
    }

    private LevelAllDto createLevelAllDto(Long currentTimestamp, Long level) {
        LevelAllDto levelAllDto = new LevelAllDto();
        levelAllDto.setLevel(level);
        levelAllDto.setMeasureTimestamp(currentTimestamp);
        return levelAllDto;
    }

    public ListActionDto findAllActionRevisions(Long id) {
        Revisions<Integer, Action> revisions = actionRepository.findRevisions(id);
        ListActionDto actionRevisionList = new ListActionDto();
        revisions.forEach(revision -> actionRevisionList.getActions().add(actionMapper.toDto(revision.getEntity())));
        return actionRevisionList;

    }
}
