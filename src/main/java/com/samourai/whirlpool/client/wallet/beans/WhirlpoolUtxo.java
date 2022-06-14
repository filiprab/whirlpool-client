package com.samourai.whirlpool.client.wallet.beans;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.whirlpool.client.wallet.data.utxoConfig.UtxoConfig;
import com.samourai.whirlpool.client.wallet.data.utxoConfig.UtxoConfigPersisted;
import com.samourai.whirlpool.client.wallet.data.utxoConfig.UtxoConfigSupplier;
import java.util.Collection;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhirlpoolUtxo {
  private static final Logger log = LoggerFactory.getLogger(WhirlpoolUtxo.class);
  private static final int MIX_MIN_CONFIRMATIONS = 1;

  private UnspentOutput utxo;
  private Integer blockHeight; // null when unconfirmed
  private BipWallet bipWallet;
  private WhirlpoolUtxoState utxoState;
  private UtxoConfigSupplier utxoConfigSupplier;

  public WhirlpoolUtxo(
      UnspentOutput utxo,
      BipWallet bipWallet,
      String poolId,
      UtxoConfigSupplier utxoConfigSupplier,
      int latestBlockHeight) {
    super();
    this.utxo = utxo;
    this.blockHeight = computeBlockHeight(utxo.confirmations, latestBlockHeight);
    this.bipWallet = bipWallet;
    this.utxoState = new WhirlpoolUtxoState(poolId);
    this.utxoConfigSupplier = utxoConfigSupplier;

    this.setMixableStatus(latestBlockHeight);
  }

  public BipAddress getBipAddress() {
    return bipWallet.getAddressAt(utxo);
  }

  private Integer computeBlockHeight(int utxoConfirmations, int latestBlockHeight) {
    if (utxoConfirmations <= 0) {
      return null;
    }
    return latestBlockHeight - utxoConfirmations;
  }

  private void setMixableStatus(int latestBlockHeight) {
    MixableStatus mixableStatus = computeMixableStatus(latestBlockHeight);
    utxoState.setMixableStatus(mixableStatus);
  }

  public void setUtxoConfirmed(UnspentOutput utxo, int latestBlockHeight) {
    this.utxo = utxo;
    this.blockHeight = computeBlockHeight(utxo.confirmations, latestBlockHeight);
    this.setMixableStatus(latestBlockHeight);
  }

  private MixableStatus computeMixableStatus(int latestBlockHeight) {
    // check pool
    if (utxoState.getPoolId() == null) {
      return MixableStatus.NO_POOL;
    }

    // check confirmations
    if (computeConfirmations(latestBlockHeight) < MIX_MIN_CONFIRMATIONS) {
      return MixableStatus.UNCONFIRMED;
    }

    // ok
    return MixableStatus.MIXABLE;
  }

  public static long sumValue(Collection<WhirlpoolUtxo> whirlpoolUtxos) {
    long sumValue = 0;
    for (WhirlpoolUtxo whirlpoolUtxo : whirlpoolUtxos) {
      sumValue += whirlpoolUtxo.getUtxo().value;
    }
    return sumValue;
  }

  public int computeConfirmations(int latestBlockHeight) {
    if (blockHeight == null) {
      return 0;
    }
    return latestBlockHeight - blockHeight;
  }

  // used by Sparrow
  public UtxoConfig getUtxoConfigOrDefault() {
    UtxoConfig utxoConfig = utxoConfigSupplier.getUtxo(utxo.tx_hash, utxo.tx_output_n);
    if (utxoConfig == null) {
      int mixsDone = WhirlpoolAccount.POSTMIX.equals(getAccount()) ? 1 : 0;
      utxoConfig = new UtxoConfigPersisted(mixsDone);
    }
    return utxoConfig;
  }

  public int getMixsDone() {
    return getUtxoConfigOrDefault().getMixsDone();
  }

  public void setMixsDone(int mixsDone) {
    utxoConfigSupplier.setMixsDone(utxo.tx_hash, utxo.tx_output_n, mixsDone);
  }

  public boolean isBlocked() {
    return getUtxoConfigOrDefault().isBlocked();
  }

  public void setBlocked(boolean blocked) {
    utxoConfigSupplier.setBlocked(utxo.tx_hash, utxo.tx_output_n, blocked);
  }

  public String getNote() {
    return getUtxoConfigOrDefault().getNote();
  }

  public void setNote(String note) {
    utxoConfigSupplier.setNote(utxo.tx_hash, utxo.tx_output_n, note);
  }

  public UnspentOutput getUtxo() {
    return utxo;
  }

  public Integer getBlockHeight() {
    return blockHeight;
  }

  public BipWallet getBipWallet() {
    return bipWallet;
  }

  public WhirlpoolAccount getAccount() {
    return bipWallet.getAccount();
  }

  public WhirlpoolUtxoState getUtxoState() {
    return utxoState;
  }

  public boolean isAccountDeposit() {
    return WhirlpoolAccount.DEPOSIT.equals(getAccount());
  }

  public boolean isAccountPremix() {
    return WhirlpoolAccount.PREMIX.equals(getAccount());
  }

  public boolean isAccountPostmix() {
    return WhirlpoolAccount.POSTMIX.equals(getAccount());
  }

  public String getPathAddress() {
    NetworkParameters params = bipWallet.getParams();
    return bipWallet.getDerivation().getPathAddress(utxo, params);
  }

  @Override
  public String toString() {
    UtxoConfig utxoConfig = getUtxoConfigOrDefault();
    return getAccount()
        + " / "
        + bipWallet.getId()
        + ": "
        + utxo.toString()
        + ", blockHeight="
        + (blockHeight != null ? blockHeight : "null")
        + ", state={"
        + utxoState
        + "}, utxoConfig={"
        + utxoConfig
        + "}";
  }

  public String getDebug() {
    StringBuilder sb = new StringBuilder();
    sb.append(toString());
    sb.append(", path=").append(getPathAddress());

    String poolId = getUtxoState().getPoolId();
    sb.append(", poolId=").append((poolId != null ? poolId : "null"));
    sb.append(", mixsDone=").append(getMixsDone());
    sb.append(", state={").append(getUtxoState().toString()).append("}");
    return sb.toString();
  }
}
