package ccom.samourai.wallet.cahoots;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.cahoots.CahootsUtxo;
import com.samourai.whirlpool.client.wallet.AbstractWhirlpoolWalletTest;
import com.samourai.whirlpool.client.wallet.beans.SamouraiAccountIndex;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class CahootsWalletTest extends AbstractWhirlpoolWalletTest {
  private Logger log = LoggerFactory.getLogger(CahootsWalletTest.class);

  public CahootsWalletTest() throws Exception {
    super();
  }

  @BeforeEach
  public void setup() throws Exception {
    super.setup();
  }

  @Override
  protected String getSeedWords() {
    return super.getSeedWords();
  }

  @Override
  protected String getPassphrase() {
    return super.getPassphrase();
  }

  @Test
  public void fetchUtxos() throws Exception {
    MyCahootsWallet cahootsWallet =
        new MyCahootsWallet(
            whirlpoolWallet.getWalletSupplier(), BIP_FORMAT.PROVIDER, params, whirlpoolWallet);

    Collection<WhirlpoolUtxo> whirlpoolUtxos = whirlpoolWallet.getUtxoSupplier().findUtxos(WhirlpoolAccount.POSTMIX);
    List<CahootsUtxo> cahootsUtxos = cahootsWallet.fetchUtxos(SamouraiAccountIndex.POSTMIX);

    // check no utxo is missing
    Assertions.assertEquals(whirlpoolUtxos.size(), cahootsUtxos.size());

    // look for specific utxo
    boolean found = false;
    for (CahootsUtxo cahootsUtxo : cahootsUtxos) {
      if (cahootsUtxo.getOutpoint().getTxHash().toString().equals("fe76a3944c662bc7ef12e369a400d0b1cd66c20d32d72e0beda8e74c8d799a62") && cahootsUtxo.getOutpoint().getTxOutputN() == 0) {
        found = true;
      }
    }
    Assertions.assertTrue(found);
  }
}
