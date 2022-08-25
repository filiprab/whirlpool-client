package com.samourai.whirlpool.client.tx0;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import java.util.Collection;
import java.util.List;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;

public class Tx0 extends Tx0Preview {
  private Collection<UnspentOutput> spendFroms;
  private Transaction tx;
  private List<TransactionOutput> premixOutputs;
  private List<TransactionOutput> changeOutputs;

  public Tx0(
      Tx0Preview tx0Preview,
      Collection<UnspentOutput> spendFroms,
      Transaction tx,
      List<TransactionOutput> premixOutputs,
      List<TransactionOutput> changeOutputs) {
    super(tx0Preview);
    this.spendFroms = spendFroms;
    this.tx = tx;
    this.premixOutputs = premixOutputs;
    this.changeOutputs = changeOutputs;
  }

  public Collection<UnspentOutput> getSpendFroms() {
    return spendFroms;
  }

  public Transaction getTx() {
    return tx;
  }

  public List<TransactionOutput> getPremixOutputs() {
    return premixOutputs;
  }

  public List<TransactionOutput> getChangeOutputs() {
    return changeOutputs;
  }
}
