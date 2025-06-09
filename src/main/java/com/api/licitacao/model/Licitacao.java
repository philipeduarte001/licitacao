package com.api.licitacao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.Column;

@Entity
@Data
public class Licitacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PORTAL")
    private String PORTAL;

    @Column(name = "EDITAL")
    private String EDITAL;

    @Column(name = "CLIENTE")
    private String CLIENTE;

    @Column(name = "OBJETO")
    private String OBJETO;

    @Column(name = "MODALIDADE")
    private String MODALIDADE;

    @Column(name = "ENTREGA")
    private String ENTREGA;

    @Column(name = "ATESTADO")
    private String ATESTADO;

    @Column(name = "AMOSTRA")
    private String AMOSTRA;

    @Column(name = "CR")
    private String CR;

    @Column(name = "IMPUGNACAO")
    private String IMPUGNACAO;

    @Column(name = "OBS")
    private String OBS;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPORTAL() {
        return PORTAL;
    }

    public void setPORTAL(String PORTAL) {
        this.PORTAL = PORTAL;
    }

    public String getEDITAL() {
        return EDITAL;
    }

    public void setEDITAL(String EDITAL) {
        this.EDITAL = EDITAL;
    }

    public String getCLIENTE() {
        return CLIENTE;
    }

    public void setCLIENTE(String CLIENTE) {
        this.CLIENTE = CLIENTE;
    }

    public String getOBJETO() {
        return OBJETO;
    }

    public void setOBJETO(String OBJETO) {
        this.OBJETO = OBJETO;
    }

    public String getMODALIDADE() {
        return MODALIDADE;
    }

    public void setMODALIDADE(String MODALIDADE) {
        this.MODALIDADE = MODALIDADE;
    }

    public String getENTREGA() {
        return ENTREGA;
    }

    public void setENTREGA(String ENTREGA) {
        this.ENTREGA = ENTREGA;
    }

    public String getATESTADO() {
        return ATESTADO;
    }

    public void setATESTADO(String ATESTADO) {
        this.ATESTADO = ATESTADO;
    }

    public String getAMOSTRA() {
        return AMOSTRA;
    }

    public void setAMOSTRA(String AMOSTRA) {
        this.AMOSTRA = AMOSTRA;
    }

    public String getCR() {
        return CR;
    }

    public void setCR(String CR) {
        this.CR = CR;
    }

    public String getIMPUGNACAO() {
        return IMPUGNACAO;
    }

    public void setIMPUGNACAO(String IMPUGNACAO) {
        this.IMPUGNACAO = IMPUGNACAO;
    }

    public String getOBS() {
        return OBS;
    }

    public void setOBS(String OBS) {
        this.OBS = OBS;
    }
}