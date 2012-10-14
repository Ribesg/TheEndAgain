package com.github.ribesg.tea.util;

import java.util.Random;

import com.github.ribesg.tea.TheEndAgain;


// Represents all config variables for one End World
public class EndWorldConfig {

    @SuppressWarnings("unused")
    private final TheEndAgain plugin;

    private boolean           regenOnStop;
    private boolean           regenOnRespawn;
    private int               actionOnRegen;
    private int               respawnTimer;
    private int               TASK_respawnTimerTask;
    private int               xpRewardingType;
    private int               xpReward;
    private int               actualNbMaxEnderDragon;
    private int               nbEnderDragon;
    private int               nbMinEnderDragon;
    private int               nbMaxEnderDragon;
    private int               enderDragonHealth;
    private int               preventPortals;
    private String            regenMessage;
    private String[]          respawnMessages;
    private String[]          expMessage1;
    private String[]          expMessage2;
    private int               customEggHandling;
    private String            eggMessage;
    private double            enderDragonDamageMultiplier;

    private int               nbEd;

    public EndWorldConfig(final TheEndAgain plugin) {
        this.plugin = plugin;
    }

    public boolean regenOnStop() {
        return this.regenOnStop;
    }

    public void setRegenOnStop(final boolean regenOnStop) {
        this.regenOnStop = regenOnStop;
    }

    public boolean regenOnRespawn() {
        return this.regenOnRespawn;
    }

    public void setRegenOnRespawn(final boolean regenOnRespawn) {
        this.regenOnRespawn = regenOnRespawn;
    }

    public int getActionOnRegen() {
        return this.actionOnRegen;
    }

    public void setActionOnRegen(final int actionOnRegen) {
        this.actionOnRegen = actionOnRegen;
    }

    public int getRespawnTimer() {
        return this.respawnTimer;
    }

    public void setRespawnTimer(final int respawnTimer) {
        this.respawnTimer = respawnTimer;
    }

    public int getNbMinEnderDragon() {
        return this.nbMinEnderDragon;
    }

    public void setNbMinEnderDragon(final int nbMinEnderDragon) {
        this.nbMinEnderDragon = nbMinEnderDragon;
    }

    public int getNbMaxEnderDragon() {
        return this.nbMaxEnderDragon;
    }

    public void setNbMaxEnderDragon(final int nbMaxEnderDragon) {
        this.nbMaxEnderDragon = nbMaxEnderDragon;
    }

    public int getRespawnTimerTask() {
        return this.TASK_respawnTimerTask;
    }

    public void setRespawnTimerTask(final int respawnTimerTask) {
        this.TASK_respawnTimerTask = respawnTimerTask;
    }

    public int getXpRewardingType() {
        return this.xpRewardingType;
    }

    public void setXpRewardingType(final int xpRewardingType) {
        this.xpRewardingType = xpRewardingType;
    }

    public int getXpReward() {
        return this.xpReward;
    }

    public void setXpReward(final int xpReward) {
        this.xpReward = xpReward;
    }

    public int getActualNbMaxEnderDragon() {
        return this.actualNbMaxEnderDragon;
    }

    public void setActualNbEnderDragon(final int actualNbEnderDragon) {
        this.actualNbMaxEnderDragon = actualNbEnderDragon;
    }

    public int getEnderDragonHealth() {
        return this.enderDragonHealth;
    }

    public void setEnderDragonHealth(final int enderDragonHealth) {
        this.enderDragonHealth = enderDragonHealth;
    }

    public int getPreventPortals() {
        return this.preventPortals;
    }

    public void setPreventPortals(final int preventPortals) {
        this.preventPortals = preventPortals;
    }

    public String getRegenMessage() {
        return this.regenMessage;
    }

    public void setRegenMessage(final String regenMessage) {
        this.regenMessage = regenMessage;
    }

    public String[] getRespawnMessages() {
        return this.respawnMessages;
    }

    public void setRespawnMessages(final String[] respawnMessages) {
        this.respawnMessages = respawnMessages;
    }

    public String[] getExpMessage1() {
        return this.expMessage1;
    }

    public void setExpMessage1(final String[] expMessage1) {
        this.expMessage1 = expMessage1;
    }

    public String[] getExpMessage2() {
        return this.expMessage2;
    }

    public void setExpMessage2(final String[] expMessage2) {
        this.expMessage2 = expMessage2;
    }

    public int getNbEnderDragon() {
        return this.nbEnderDragon;
    }

    public void setNbEnderDragon(final int nbEnderDragon) {
        this.nbEnderDragon = nbEnderDragon;
    }

    public boolean newActualNumber() {
        // RETURN : Are nbMin and nbMax valids ?
        if (this.nbMaxEnderDragon - this.nbMinEnderDragon < 0) {
            this.nbMinEnderDragon = 1;
            this.nbMaxEnderDragon = 1;
            this.actualNbMaxEnderDragon = 1;
            return false;
        } else {
            this.actualNbMaxEnderDragon = this.nbMinEnderDragon + new Random().nextInt(this.nbMaxEnderDragon - this.nbMinEnderDragon + 1);
            return true;
        }
    }

    public int getNbEd() {
        return this.nbEd;
    }

    public void setNbEd(final int nbEd) {
        this.nbEd = nbEd;
    }

    public int getCustomEggHandling() {
        return this.customEggHandling;
    }

    public void setCustomEggHandling(final int customEggHandling) {
        this.customEggHandling = customEggHandling;
    }

    public String getEggMessage() {
        return this.eggMessage;
    }

    public void setEggMessage(final String eggMessage) {
        this.eggMessage = eggMessage;
    }

    public double getEnderDragonDamageMultiplier() {
        return this.enderDragonDamageMultiplier;
    }

    public void setEnderDragonDamageMultiplier(final double enderDragonDamageMultiplier) {
        this.enderDragonDamageMultiplier = enderDragonDamageMultiplier;
    }
}
