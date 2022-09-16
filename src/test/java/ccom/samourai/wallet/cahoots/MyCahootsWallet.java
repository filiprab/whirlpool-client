package ccom.samourai.wallet.cahoots;

import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.bipWallet.WalletSupplier;
import com.samourai.wallet.cahoots.CahootsUtxo;
import com.samourai.wallet.cahoots.CahootsWallet;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.whirlpool.client.wallet.WhirlpoolWallet;
import com.samourai.whirlpool.client.wallet.beans.SamouraiAccountIndex;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolAccount;
import com.samourai.whirlpool.client.wallet.beans.WhirlpoolUtxo;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

public class MyCahootsWallet extends CahootsWallet {
  private WhirlpoolWallet whirlpoolWallet;

  public MyCahootsWallet(
      WalletSupplier walletSupplier,
      BipFormatSupplier bipFormatSupplier,
      NetworkParameters params,
      WhirlpoolWallet whirlpoolWallet) {
    super(walletSupplier, bipFormatSupplier, params);
    this.whirlpoolWallet = whirlpoolWallet;
  }

  @Override
  public List<CahootsUtxo> fetchUtxos(int account) {
    WhirlpoolAccount whirlpoolAccount =
        account == SamouraiAccountIndex.DEPOSIT
            ? WhirlpoolAccount.DEPOSIT
            : WhirlpoolAccount.POSTMIX;
    Collection<WhirlpoolUtxo> utxos = whirlpoolWallet.getUtxoSupplier().findUtxos(whirlpoolAccount);

    List<CahootsUtxo> cahootsUtxos = new LinkedList<>();
    for (WhirlpoolUtxo utxo : utxos) {
      try {
        MyTransactionOutPoint outpoint = utxo.getUtxo().computeOutpoint(getParams());
        ECKey ecKey =
            ECKey.fromPrivate(
                whirlpoolWallet
                    .getUtxoSupplier()
                    ._getPrivKey(utxo.getUtxo().tx_hash, utxo.getUtxo().tx_output_n));
        String path = utxo.getUtxo().getPath();
        if (path != null && ecKey != null) {
          CahootsUtxo cahootsUtxo = new CahootsUtxo(outpoint, path, ecKey);
          cahootsUtxos.add(cahootsUtxo);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return cahootsUtxos;
  }

  @Override
  public long fetchFeePerB() {
    return 1;
  }
}
