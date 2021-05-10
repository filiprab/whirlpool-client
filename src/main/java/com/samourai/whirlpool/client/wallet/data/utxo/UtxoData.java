package com.samourai.whirlpool.client.wallet.data.utxo;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.api.backend.beans.WalletResponse;
import com.samourai.whirlpool.client.utils.ClientUtils;
import com.samourai.whirlpool.client.wallet.beans.*;
import com.samourai.whirlpool.client.wallet.data.minerFee.WalletSupplier;
import java.util.*;
import java8.util.function.Predicate;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtxoData {
  private static final Logger log = LoggerFactory.getLogger(UtxoData.class);

  private final Map<String, WhirlpoolUtxo> utxos;
  private final Map<WhirlpoolAccount, List<WalletResponse.Tx>> txsByAccount;
  private final WhirlpoolUtxoChanges utxoChanges;
  private final Map<WhirlpoolAccount, Long> balanceByAccount;
  private final long balanceTotal;

  protected UtxoData(
      WalletSupplier walletSupplier,
      UtxoConfigSupplier utxoConfigSupplier,
      WalletResponse walletResponse,
      Map<String, WhirlpoolUtxo> previousUtxos) {
    // txs
    final Map<WhirlpoolAccount, List<WalletResponse.Tx>> freshTxs =
        new LinkedHashMap<WhirlpoolAccount, List<WalletResponse.Tx>>();
    for (WhirlpoolAccount account : WhirlpoolAccount.values()) {
      freshTxs.put(account, new LinkedList<WalletResponse.Tx>());
    }
    for (WalletResponse.Tx tx : walletResponse.txs) {
      Collection<WhirlpoolAccount> txAccounts = findTxAccounts(tx, walletSupplier);
      for (WhirlpoolAccount txAccount : txAccounts) {
        freshTxs.get(txAccount).add(tx);
      }
    }
    this.txsByAccount = freshTxs;

    // fresh utxos
    final Map<String, UnspentOutput> freshUtxos = new LinkedHashMap<String, UnspentOutput>();
    for (UnspentOutput utxo : walletResponse.unspent_outputs) {
      String utxoKey = ClientUtils.utxoToKey(utxo);
      freshUtxos.put(utxoKey, utxo);
    }

    // replace utxos
    boolean isFirstFetch = false;
    if (previousUtxos == null) {
      previousUtxos = new LinkedHashMap<String, WhirlpoolUtxo>();
      isFirstFetch = true;
    }

    this.utxos = new LinkedHashMap<String, WhirlpoolUtxo>();
    this.utxoChanges = new WhirlpoolUtxoChanges(isFirstFetch);

    // add existing utxos
    for (WhirlpoolUtxo whirlpoolUtxo : previousUtxos.values()) {
      String key = ClientUtils.utxoToKey(whirlpoolUtxo.getUtxo());

      UnspentOutput freshUtxo = freshUtxos.get(key);
      if (freshUtxo != null) {
        UnspentOutput oldUtxo = whirlpoolUtxo.getUtxo();

        // update utxo if changed
        if (freshUtxo.confirmations != oldUtxo.confirmations) {
          whirlpoolUtxo._setUtxo(freshUtxo);
          utxoChanges.getUtxosUpdated().add(whirlpoolUtxo);
        }
        // add
        utxos.put(key, whirlpoolUtxo);
      } else {
        // obsolete
        utxoChanges.getUtxosRemoved().add(whirlpoolUtxo);
      }
    }

    // add missing utxos
    for (Map.Entry<String, UnspentOutput> e : freshUtxos.entrySet()) {
      String key = e.getKey();
      if (!previousUtxos.containsKey(key)) {
        UnspentOutput utxo = e.getValue();
        try {
          // find account
          String zpub = utxo.xpub.m;
          WhirlpoolAccount account = walletSupplier.getAccountByZpub(zpub);
          if (account == null) {
            throw new Exception("Unknown account for utxo: " + utxo);
          }

          // add missing
          WhirlpoolUtxo whirlpoolUtxo =
              new WhirlpoolUtxo(utxo, account, WhirlpoolUtxoStatus.READY, utxoConfigSupplier);
          if (!isFirstFetch) {
            // set lastActivity when utxo is detected but ignore on first fetch
            whirlpoolUtxo.getUtxoState().setLastActivity();
            if (log.isDebugEnabled()) {
              log.debug("+utxo: " + whirlpoolUtxo);
            }
          }
          utxoChanges.getUtxosAdded().add(whirlpoolUtxo);
          utxos.put(key, whirlpoolUtxo);
        } catch (Exception ee) {
          log.error("error loading new utxo", ee);
        }
      }
    }

    // compute balances
    this.balanceByAccount = new LinkedHashMap<WhirlpoolAccount, Long>();
    long total = 0;
    for (WhirlpoolAccount account : WhirlpoolAccount.getListByActive(true)) {
      Collection<WhirlpoolUtxo> utxosForAccount = findUtxos(false, account);
      long balance = ClientUtils.computeUtxosBalance(utxosForAccount);
      balanceByAccount.put(account, balance);
      total += balance;
    }
    this.balanceTotal = total;

    if (log.isDebugEnabled()) {
      log.debug("utxos: " + previousUtxos.size() + " => " + utxos.size() + ", " + utxoChanges);
    }
  }

  private Collection<WhirlpoolAccount> findTxAccounts(
      WalletResponse.Tx tx, WalletSupplier walletSupplier) {
    Set<WhirlpoolAccount> accounts = new LinkedHashSet<WhirlpoolAccount>();
    // verify inputs
    for (WalletResponse.TxInput input : tx.inputs) {
      if (input.prev_out != null) {
        WhirlpoolAccount whirlpoolAccount = walletSupplier.getAccountByZpub(input.prev_out.xpub.m);
        if (whirlpoolAccount != null) {
          accounts.add(whirlpoolAccount);
        }
      }
    }
    // verify outputs
    for (WalletResponse.TxOutput output : tx.out) {
      WhirlpoolAccount whirlpoolAccount = walletSupplier.getAccountByZpub(output.xpub.m);
      if (whirlpoolAccount != null) {
        accounts.add(whirlpoolAccount);
      }
    }
    return accounts;
  }

  // utxos

  public Map<String, WhirlpoolUtxo> getUtxos() {
    return utxos;
  }

  public Collection<WalletResponse.Tx> findTxs(WhirlpoolAccount whirlpoolAccount) {
    return txsByAccount.get(whirlpoolAccount);
  }

  public WhirlpoolUtxoChanges getUtxoChanges() {
    return utxoChanges;
  }

  public WhirlpoolUtxo findByUtxoKey(String utxoHash, int utxoIndex) {
    String utxoKey = ClientUtils.utxoToKey(utxoHash, utxoIndex);
    return utxos.get(utxoKey);
  }

  public Collection<WhirlpoolUtxo> findUtxos(
      final boolean excludeNoPool, final WhirlpoolAccount... whirlpoolAccounts) {
    return StreamSupport.stream(utxos.values())
        .filter(
            new Predicate<WhirlpoolUtxo>() {
              @Override
              public boolean test(WhirlpoolUtxo whirlpoolUtxo) {
                if (!ArrayUtils.contains(whirlpoolAccounts, whirlpoolUtxo.getAccount())) {
                  return false;
                }
                if (excludeNoPool) {
                  if (whirlpoolUtxo.getUtxoState() != null
                      && MixableStatus.NO_POOL.equals(
                          whirlpoolUtxo.getUtxoState().getMixableStatus())) {
                    return false;
                  }
                }
                return true;
              }
            })
        .collect(Collectors.<WhirlpoolUtxo>toList());
  }

  // balances

  public long getBalance(WhirlpoolAccount account) {
    return balanceByAccount.get(account);
  }

  public long getBalanceTotal() {
    return balanceTotal;
  }

  @Override
  public String toString() {
    return utxos.size() + " utxos";
  }
}
