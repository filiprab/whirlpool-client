package com.samourai.whirlpool.client.wallet.beans;

import com.samourai.whirlpool.client.wallet.data.utxo.UtxoConfigPersisted;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoConfigSupplier;

public abstract class WhirlpoolUtxoConfig {
  public static final int MIXS_TARGET_UNLIMITED = 0;

  public WhirlpoolUtxoConfig() {}

  protected abstract UtxoConfigPersisted getUtxoConfigPersisted();

  protected abstract UtxoConfigSupplier getUtxoConfigSupplier();

  public String getPoolId() {
    return getUtxoConfigPersisted().getPoolId();
  }

  private void onChange() {
    getUtxoConfigSupplier().setLastChange();
  }

  public void setPoolId(String poolId) {
    getUtxoConfigPersisted().setPoolId(poolId);
    onChange();
  }

  public Integer getMixsTarget() {
    return getUtxoConfigPersisted().getMixsTarget();
  }

  public int getMixsTargetOrDefault(int mixsTargetMin) {
    Integer mixsTarget = getMixsTarget();
    if (mixsTargetMin == WhirlpoolUtxoConfig.MIXS_TARGET_UNLIMITED
        || (mixsTarget != null && mixsTarget == WhirlpoolUtxoConfig.MIXS_TARGET_UNLIMITED)) {
      return WhirlpoolUtxoConfig.MIXS_TARGET_UNLIMITED;
    }

    int minimum = Math.max(mixsTargetMin, getMixsDone());
    if (mixsTarget == null) {
      return minimum;
    }
    return Math.max(mixsTarget, minimum);
  }

  public boolean isDone(int mixsTargetMin) {
    int mixsTargetOrDefault = getMixsTargetOrDefault(mixsTargetMin);
    return (getMixsDone() >= mixsTargetOrDefault
        && mixsTargetOrDefault != WhirlpoolUtxoConfig.MIXS_TARGET_UNLIMITED);
  }

  public void setMixsTarget(Integer mixsTarget) {
    getUtxoConfigPersisted().setMixsTarget(mixsTarget);
    onChange();
  }

  public int getMixsDone() {
    return getUtxoConfigPersisted().getMixsDone();
  }

  public void setMixsDone(int mixsDone) {
    getUtxoConfigPersisted().setMixsDone(mixsDone);
    onChange();
  }

  public void incrementMixsDone() {
    getUtxoConfigPersisted().incrementMixsDone();
    onChange();
  }

  @Override
  public String toString() {
    return getUtxoConfigPersisted().toString();
  }
}
