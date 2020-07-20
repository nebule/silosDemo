package com.nebojsa.silos.rest;

import com.nebojsa.silos.dto.ActionDto;
import com.nebojsa.silos.dto.ListActionDto;
import com.nebojsa.silos.exceptions.ErrorMessageDto;
import com.nebojsa.silos.exceptions.SiloCapacityFullException;
import com.nebojsa.silos.exceptions.SiloNotEnoughLevelException;
import com.nebojsa.silos.service.ActionService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("action")
@Log
public class ActionController {

    private final ActionService actionService;

    @Autowired
    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<ActionDto> get(@PathVariable("id") Long id) {
        ActionDto actionDto = actionService.getById(id);
        return ResponseEntity.ok(actionDto);
    }

    @PostMapping("/do")
    public ResponseEntity<ActionDto> add(@RequestBody ActionDto actionDto) {
        ActionDto responseActionDto = actionService.doAction(actionDto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("action/get/{id}")
                .buildAndExpand(responseActionDto.getId()).toUri();

        return ResponseEntity.created(location).body(responseActionDto);
    }


    @PutMapping("/update")
    public ResponseEntity<ActionDto> update(@RequestBody ActionDto actionDto) {
        ActionDto updated = actionService.update(actionDto);
        return ResponseEntity.ok(updated);
    }

    @ExceptionHandler({SiloCapacityFullException.class, SiloNotEnoughLevelException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleException(Exception exception) {
        ErrorMessageDto errorMessageDto = new ErrorMessageDto();
        errorMessageDto.setMessage(exception.getMessage());
        errorMessageDto.setStatus(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(errorMessageDto, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/versions/{id}")
    public ResponseEntity<ListActionDto> getAllVersions(@PathVariable("id") Long id) {
        ListActionDto allActionRevisions = actionService.findAllActionRevisions(id);
        return ResponseEntity.ok(allActionRevisions);
    }
}
