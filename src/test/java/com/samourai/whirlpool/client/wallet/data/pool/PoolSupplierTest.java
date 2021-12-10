package com.samourai.whirlpool.client.wallet.data.pool;

import com.samourai.whirlpool.client.test.AbstractTest;
import com.samourai.whirlpool.client.tx0.Tx0Preview;
import com.samourai.whirlpool.client.whirlpool.beans.Pool;
import com.samourai.whirlpool.protocol.websocket.notifications.MixStatus;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PoolSupplierTest extends AbstractTest {
  private ExpirablePoolSupplier supplier;

  @Before
  public void setup() throws Exception {
    this.supplier = mockPoolSupplier();
  }

  @Test
  public void testValid() throws Exception {
    // valid
    doTest();

    // non-existing pool
    Assert.assertNull(supplier.findPoolById("foo"));
  }

  private void doTest() throws Exception {
    supplier.load();

    // verify getPools
    Collection<Pool> getPools = supplier.getPools();
    Assert.assertEquals(4, getPools.size());

    // verify findPoolById
    Pool pool01 = supplier.findPoolById("0.01btc");
    Assert.assertEquals("0.01btc", pool01.getPoolId());
    Assert.assertEquals(1000000, pool01.getDenomination());
    Assert.assertEquals(50000, pool01.getFeeValue());
    Assert.assertEquals(1000170, pool01.getMustMixBalanceMin());
    Assert.assertEquals(1009690, pool01.getMustMixBalanceCap());
    Assert.assertEquals(1019125, pool01.getMustMixBalanceMax());
    Assert.assertEquals(5, pool01.getMinAnonymitySet());
    Assert.assertEquals(2, pool01.getMinMustMix());
    Assert.assertEquals(70, pool01.getTx0MaxOutputs());
    Assert.assertEquals(180, pool01.getNbRegistered());
    Assert.assertEquals(5, pool01.getMixAnonymitySet());
    Assert.assertEquals(MixStatus.CONFIRM_INPUT, pool01.getMixStatus());
    Assert.assertEquals(672969, pool01.getElapsedTime());
    Assert.assertEquals(2, pool01.getNbConfirmed());

    // verify getTx0PreviewMin
    Tx0Preview tx0Preview = pool01.getTx0PreviewMin();
    Assert.assertEquals(1050577, tx0Preview.getTotalValue());
    Assert.assertEquals(1050577, tx0Preview.getSpendValue());
    Assert.assertEquals(1000302, tx0Preview.getPremixValue());
    Assert.assertEquals(50000, tx0Preview.getFeeValue());
    Assert.assertEquals(0, tx0Preview.getFeeChange());
    Assert.assertEquals(275, tx0Preview.getTx0Size());
    Assert.assertEquals(1, tx0Preview.getNbPremix());
    Assert.assertEquals(302, tx0Preview.getMixMinerFee());
    Assert.assertEquals(1, tx0Preview.getMixMinerFeePrice());
    Assert.assertEquals(275, tx0Preview.getTx0MinerFee());
    Assert.assertEquals(1, tx0Preview.getTx0MinerFeePrice());
    Assert.assertEquals(0, tx0Preview.getChangeValue());
    Assert.assertEquals("0.01btc", tx0Preview.getPool().getPoolId());
    Assert.assertEquals(1050577, pool01.getTx0PreviewMinSpendValue());
  }
}
