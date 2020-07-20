package com.nebojsa.silos.rest;

import com.nebojsa.silos.dto.LevelAllDto;
import com.nebojsa.silos.dto.LevelDto;
import com.nebojsa.silos.dto.SiloDto;
import com.nebojsa.silos.exceptions.ErrorMessageDto;
import com.nebojsa.silos.service.ActionService;
import com.nebojsa.silos.service.SiloService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("silo")
@Log
public class SiloController {

    private final SiloService siloService;

    private final ActionService actionService;

    @Autowired
    public SiloController(SiloService siloService, ActionService actionService) {
        this.siloService = siloService;
        this.actionService = actionService;
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<SiloDto> get(@PathVariable("id") Long id) {
        SiloDto siloDto = siloService.getById(id);
        return ResponseEntity.ok(siloDto);
    }


    @PostMapping("/create")
    public ResponseEntity<SiloDto> add(@RequestBody SiloDto siloDto) {
        SiloDto responseSiloDto = siloService.create(siloDto);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("silo/get/{id}")
                .buildAndExpand(responseSiloDto.getId()).toUri();

        return ResponseEntity.created(location).body(responseSiloDto);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SiloDto> update(@RequestBody SiloDto siloDto, @PathVariable("id") Long id) {
        SiloDto responseSiloDto = siloService.update(siloDto, id);
        return ResponseEntity.ok(responseSiloDto);
    }


    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        siloService.delete(id);
    }

    @GetMapping("/level/{id}")
    public ResponseEntity<LevelDto> level(@PathVariable("id") Long id) {
        LevelDto level = actionService.calculateCurrentLevel(id);
        return ResponseEntity.ok(level);
    }

    @GetMapping("/level/{id}/{timestamp}")
    public ResponseEntity<LevelDto> levelOnTimestamp(@PathVariable("id") Long id, @PathVariable("timestamp") Long timestamp) {
        LevelDto level = actionService.calculateLevel(id, timestamp);
        return ResponseEntity.ok(level);
    }

    @GetMapping("/level/all")
    public ResponseEntity<LevelAllDto> level() {
        LevelAllDto level = actionService.calculateCurrentLevelAll();
        return ResponseEntity.ok(level);
    }

    @GetMapping("/level/all/{timestamp}")
    public ResponseEntity<LevelAllDto> levelOnTimestamp(@PathVariable("timestamp") Long timestamp) {
        LevelAllDto level = actionService.calculateLevelAllAtTimestamp(timestamp);
        return ResponseEntity.ok(level);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handleException(Exception exception) {
        ErrorMessageDto errorMessageDto = new ErrorMessageDto();
        errorMessageDto.setMessage(exception.getMessage());
        errorMessageDto.setStatus(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(errorMessageDto, HttpStatus.BAD_REQUEST);
    }


}
