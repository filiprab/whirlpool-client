package com.samourai.whirlpool.client.wallet.data.minerFee;

import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.api.backend.beans.WalletResponse;
import com.samourai.whirlpool.client.event.UtxosRequestEvent;
import com.samourai.whirlpool.client.tx0.Tx0ParamService;
import com.samourai.whirlpool.client.wallet.WhirlpoolEventService;
import com.samourai.whirlpool.client.wallet.WhirlpoolWalletConfig;
import com.samourai.whirlpool.client.wallet.data.ExpirableSupplier;
import com.samourai.whirlpool.client.wallet.data.LoadableSupplier;
import com.samourai.whirlpool.client.wallet.data.pool.PoolSupplier;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoConfigPersister;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoConfigSupplier;
import com.samourai.whirlpool.client.wallet.data.utxo.UtxoSupplier;
import com.samourai.whirlpool.client.wallet.data.walletState.WalletStateSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletDataSupplier extends ExpirableSupplier<WalletResponse>
    implements LoadableSupplier {
  private static final Logger log = LoggerFactory.getLogger(WalletDataSupplier.class);

  private final BackendApi backendApi;
  private final WalletSupplier walletSupplier;
  private final WalletStateSupplier walletStateSupplier;

  private final MinerFeeSupplier minerFeeSupplier;
  private final ChainSupplier chainSupplier;
  private final UtxoSupplier utxoSupplier;
  private final UtxoConfigSupplier utxoConfigSupplier;
  private final Tx0ParamService tx0ParamService;

  public WalletDataSupplier(
      int refreshUtxoDelay,
      WalletSupplier walletSupplier,
      PoolSupplier poolSupplier,
      String utxoConfigFileName,
      WhirlpoolWalletConfig config) {
    super(refreshUtxoDelay, null, log);
    this.backendApi = config.getBackendApi();
    this.walletSupplier = walletSupplier;
    this.walletStateSupplier = walletSupplier.getWalletStateSupplier();

    this.minerFeeSupplier =
        new MinerFeeSupplier(config.getFeeMin(), config.getFeeMax(), config.getFeeFallback());

    this.chainSupplier = new ChainSupplier();

    this.tx0ParamService = new Tx0ParamService(minerFeeSupplier, config);

    this.utxoConfigSupplier =
        new UtxoConfigSupplier(
            new UtxoConfigPersister(utxoConfigFileName), poolSupplier, tx0ParamService);

    this.utxoSupplier =
        new UtxoSupplier(walletSupplier, utxoConfigSupplier, this, config.getNetworkParameters());
  }

  protected WalletResponse fetchWalletResponse() throws Exception {
    String[] utxoZpubs = walletSupplier.getZpubs(false);
    return backendApi.fetchWallet(utxoZpubs);
  }

  @Override
  protected WalletResponse fetch() throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("fetching...");
    }
    WhirlpoolEventService.getInstance().post(new UtxosRequestEvent());
    WalletResponse walletResponse = fetchWalletResponse();

    // update minerFeeSupplier
    try {
      minerFeeSupplier._setValue(walletResponse);
    } catch (Exception e) {
      log.error("minerFeeSupplier._setValue failed => using fallback value", e);
    }

    // update chainSupplier
    chainSupplier._setValue(walletResponse);

    // update utxoSupplier
    utxoSupplier._setValue(walletResponse);

    // update walletStateSupplier
    walletStateSupplier._setValue(walletResponse);

    return walletResponse;
  }

  public MinerFeeSupplier getMinerFeeSupplier() {
    return minerFeeSupplier;
  }

  public ChainSupplier getChainSupplier() {
    return chainSupplier;
  }

  public UtxoSupplier getUtxoSupplier() {
    return utxoSupplier;
  }

  public UtxoConfigSupplier getUtxoConfigSupplier() {
    return utxoConfigSupplier;
  }

  public Tx0ParamService getTx0ParamService() {
    return tx0ParamService;
  }
}
