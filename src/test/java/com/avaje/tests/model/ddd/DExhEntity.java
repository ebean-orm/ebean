package com.avaje.tests.model.ddd;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import com.avaje.tests.model.ivo.ExhangeCMoneyRate;
import com.avaje.tests.model.ivo.Oid;

@Entity
public class DExhEntity {

    @Id
    Oid<DExhEntity> oid;
    
    ExhangeCMoneyRate exhange;

    @Version
    Timestamp lastUpdated;

    public Oid<DExhEntity> getOid() {
        return oid;
    }

    public void setOid(Oid<DExhEntity> oid) {
        this.oid = oid;
    }

    public ExhangeCMoneyRate getExhange() {
        return exhange;
    }

    public void setExhange(ExhangeCMoneyRate exhange) {
        this.exhange = exhange;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
}
