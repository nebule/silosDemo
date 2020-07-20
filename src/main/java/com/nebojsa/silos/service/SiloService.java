package com.nebojsa.silos.service;

import com.nebojsa.silos.dto.SiloDto;
import com.nebojsa.silos.dto.SiloMapper;
import com.nebojsa.silos.entity.Silo;
import com.nebojsa.silos.repository.SiloRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log
public class SiloService {

    private final SiloRepository siloRepository;

    private final SiloMapper siloMapper;

    @Autowired
    public SiloService(SiloRepository siloRepository, SiloMapper siloMapper) {
        this.siloRepository = siloRepository;
        this.siloMapper = siloMapper;
    }

    public Silo findById(Long id) {
        return siloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Silo for id=" + id + " Not found"));
    }

    public SiloDto getById(Long id) {
        return siloMapper.toDto(findById(id));
    }

    public SiloDto create(SiloDto siloDto) {
        Silo silo = new Silo();
        silo.setName(siloDto.getName());
        silo.setCapacity(siloDto.getCapacity());
        siloRepository.save(silo);
        SiloDto responseSiloDto = siloMapper.toDto(silo);
        log.info("creating silo: " + responseSiloDto.toString());
        return responseSiloDto;
    }

    public SiloDto update(SiloDto siloDto, long siloId) {
        Silo silo = findById(siloId);
        silo.setName(siloDto.getName());
        silo.setCapacity(siloDto.getCapacity());
        siloRepository.save(silo);
        SiloDto responseSiloDto = siloMapper.toDto(silo);
        log.info("updating silo: " + responseSiloDto.toString());
        return responseSiloDto;
    }

    public List<Silo> findAll() {
        return siloRepository.findAll();
    }

    public void delete(Long siloId) {
        siloRepository.deleteById(siloId);
    }

}
