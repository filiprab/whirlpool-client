package com.samourai.whirlpool.client.wallet.beans;

import java.util.Optional;

import com.samourai.dex.config.DexConfigProvider;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

public enum WhirlpoolServer {
  TESTNET(
          DexConfigProvider.getInstance().getSamouraiConfig().getWhirlpoolServerTestnetClear(),
          DexConfigProvider.getInstance().getSamouraiConfig().getBackendServerTestnetOnion(),
          TestNet3Params.get()),
  INTEGRATION(
          DexConfigProvider.getInstance().getSamouraiConfig().getWhirlpoolServerIntegrationClear(),
          DexConfigProvider.getInstance().getSamouraiConfig().getWhirlpoolServerIntegrationOnion(),
          TestNet3Params.get()),
  MAINNET(
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerMainnetClear(),
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerMainnetOnion(),
          MainNetParams.get()),
  LOCAL_TESTNET("http://127.0.0.1:8080", "http://127.0.0.1:8080", TestNet3Params.get());

  private String serverUrlClear;
  private String serverUrlOnion;
  private NetworkParameters params;

  WhirlpoolServer(String serverUrlClear, String serverUrlOnion, NetworkParameters params) {
    this.serverUrlClear = serverUrlClear;
    this.serverUrlOnion = serverUrlOnion;
    this.params = params;
  }

  public String getServerUrlClear() {
    return serverUrlClear;
  }

  public String getServerUrlOnion() {
    return serverUrlOnion;
  }

  public String getServerUrl(boolean onion) {
    String serverUrl = onion ? getServerUrlOnion() : getServerUrlClear();
    return serverUrl;
  }

  public NetworkParameters getParams() {
    return params;
  }

  public static Optional<WhirlpoolServer> find(String value) {
    try {
      return Optional.of(valueOf(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
