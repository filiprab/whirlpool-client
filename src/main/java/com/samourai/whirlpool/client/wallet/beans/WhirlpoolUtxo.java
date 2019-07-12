package com.samourai.whirlpool.client.wallet.beans;

import com.samourai.wallet.api.backend.beans.UnspentResponse.UnspentOutput;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.mix.listener.MixStep;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;

public class WhirlpoolUtxo {
  private UnspentOutput utxo;
  private WhirlpoolAccount account;
  private WhirlpoolUtxoStatus status;
  private MixStep mixStep;
  private MixableStatus mixableStatus;
  private WhirlpoolWallet wallet;

  private Integer progressPercent;
  private String message;
  private String error;
  private Long lastActivity;
  private Long lastError;

  public WhirlpoolUtxo(
      UnspentOutput utxo,
      WhirlpoolAccount account,
      WhirlpoolUtxoStatus status,
      WhirlpoolWallet wallet) {
    this.utxo = utxo;
    this.account = account;
    this.status = status;
    this.mixStep = null;
    this.mixableStatus = null;
    this.wallet = wallet;

    this.progressPercent = null;
    this.message = null;
    this.error = null;
    this.lastActivity = null;
    this.lastError = null;
  }

  public UnspentOutput getUtxo() {
    return utxo;
  }

  public WhirlpoolAccount getAccount() {
    return account;
  }

  public WhirlpoolUtxoStatus getStatus() {
    return status;
  }

  public MixStep getMixStep() {
    return mixStep;
  }

  public Integer getProgressPercent() {
    return progressPercent;
  }

  public void setMessage(String message) {
    this.message = message;
    setLastActivity();
  }

  public boolean hasMessage() {
    return message != null;
  }

  public String getMessage() {
    return message;
  }

  public void setError(Exception e) {
    String message = NotifiableException.computeNotifiableException(e).getMessage();
    setError(message);
  }

  public void setError(String error) {
    this.error = error;
    setLastActivity();
    setLastError();
  }

  public boolean hasError() {
    return error != null;
  }

  public String getError() {
    return error;
  }

  public void setStatus(
      WhirlpoolUtxoStatus status,
      boolean updateLastActivity,
      MixStep mixStep,
      Integer progressPercent) {
    this.status = status;
    this.mixStep = mixStep;
    this.progressPercent = progressPercent;
    if (!WhirlpoolUtxoStatus.MIX_QUEUE.equals(status)) {
      this.error = null;
    }
    if (updateLastActivity) {
      setLastActivity();
    }
  }

  public void setStatus(WhirlpoolUtxoStatus status, boolean updateLastActivity) {
    setStatus(status, updateLastActivity, null, null);
  }

  public void setStatus(
      WhirlpoolUtxoStatus status, boolean updateLastActivity, int progressPercent) {
    setStatus(status, updateLastActivity, null, progressPercent);
  }

  public MixableStatus getMixableStatus() {
    return mixableStatus;
  }

  public void setMixableStatus(MixableStatus mixableStatus) {
    this.mixableStatus = mixableStatus;
  }

  public void setUtxo(UnspentOutput utxo) {
    this.utxo = utxo;
  }

  public Long getLastActivity() {
    return lastActivity;
  }

  public void setLastActivity() {
    this.lastActivity = System.currentTimeMillis();
  }

  public void setLastError() {
    this.lastError = System.currentTimeMillis();
  }

  public Long getLastError() {
    return lastError;
  }

  public void setLastError(Long lastError) {
    this.lastError = lastError;
  }

  public WhirlpoolUtxoConfig getUtxoConfig() {
    return wallet.getUtxoConfig(this);
  }

  @Override
  public String toString() {
    String progressStr = "";
    if (progressPercent != null) {
      progressStr += progressPercent + "%";
    }

    return "account="
        + account
        + ", status="
        + status
        + (!progressStr.isEmpty() ? " (" + progressStr + ")" : "")
        + ", mixStep="
        + (mixStep != null ? mixStep : "null")
        + ", mixableStatus="
        + (mixableStatus != null ? mixableStatus : "null")
        + (hasMessage() ? ", message=" + message : "")
        + (hasError() ? ", error=" + error : "")
        + (lastError != null ? ", lastError=" + lastError : "")
        + ", utxo="
        + utxo.toString();
  }
}
