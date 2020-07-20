package com.nebojsa.silos.entity;

import com.nebojsa.silos.constants.ActionName;
import lombok.Data;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


@Entity
@Data
@Audited
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private long amount;

    @ManyToOne
    private Silo from;

    @ManyToOne
    private Silo to;

    @Column
    private ActionName actionName;

    @Column
    private Long updatedInMillis;
}
