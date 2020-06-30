package com.samourai.whirlpool.client.wallet.data.utxo;

import com.samourai.whirlpool.client.wallet.data.PersistableData;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java8.util.function.Function;
import java8.util.function.Predicate;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtxoConfigData extends PersistableData {
  private static final Logger log = LoggerFactory.getLogger(UtxoConfigData.class);

  private Map<String, UtxoConfigPersisted> utxoConfigs;

  protected UtxoConfigData() {
    this(new LinkedHashMap<String, UtxoConfigPersisted>());
  }

  protected UtxoConfigData(Map<String, UtxoConfigPersisted> utxoConfigs) {
    super();
    this.utxoConfigs = utxoConfigs;
  }

  protected Map<String, UtxoConfigPersisted> getUtxoConfigs() {
    return utxoConfigs;
  }

  public synchronized void add(String key, UtxoConfigPersisted utxoConfig) {
    utxoConfigs.put(key, utxoConfig);
    setLastChange();
  }

  protected synchronized int cleanup(final Collection<String> validKeys) {
    // remove obsolete utxoConfigs
    Map<String, UtxoConfigPersisted> newUtxoConfigs =
        StreamSupport.stream(utxoConfigs.entrySet())
            .filter(
                new Predicate<Map.Entry<String, UtxoConfigPersisted>>() {
                  @Override
                  public boolean test(Map.Entry<String, UtxoConfigPersisted> e) {
                    return validKeys.contains(e.getKey());
                  }
                })
            .collect(
                Collectors.toMap(
                    new Function<Map.Entry<String, UtxoConfigPersisted>, String>() {
                      @Override
                      public String apply(Map.Entry<String, UtxoConfigPersisted> e) {
                        return e.getKey();
                      }
                    },
                    new Function<Map.Entry<String, UtxoConfigPersisted>, UtxoConfigPersisted>() {
                      @Override
                      public UtxoConfigPersisted apply(Map.Entry<String, UtxoConfigPersisted> e) {
                        return e.getValue();
                      }
                    }));

    int nbCleaned = utxoConfigs.size() - newUtxoConfigs.size();
    this.utxoConfigs = newUtxoConfigs;
    if (nbCleaned > 0) {
      setLastChange();
    }
    return nbCleaned;
  }

  public UtxoConfigPersisted getUtxoConfig(String key) {
    return utxoConfigs.get(key);
  }

  @Override
  protected void setLastChange() {
    super.setLastChange();
  }

  @Override
  public String toString() {
    return utxoConfigs.size() + " utxoConfigs";
  }
}
