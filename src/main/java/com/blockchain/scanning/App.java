package com.blockchain.scanning;
import java.math.BigInteger;
import java.util.List;
import com.blockchain.scanning.biz.thread.EventThreadPool;
import com.blockchain.scanning.commons.codec.EthAbiCodec;
import com.blockchain.scanning.commons.config.rpcinit.impl.EthRpcInit;
import com.blockchain.scanning.monitor.EthMonitorEvent;
import com.blockchain.scanning.monitor.EventOne;
import net.osslabz.evm.abi.decoder.AbiDecoder;
import net.osslabz.evm.abi.decoder.DecodedFunctionCall;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.TypeReference;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Function;
import java.util.List;



/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws Exception {

        // Initialize the thread pool, the number of core threads must be >= the number of chains you want to scan, it is recommended to equal the number of chains to be scanned
        EventThreadPool.init(1);

// Open a scan task, if you want to scan multiple chains, you can open multiple tasks,
// by copying the following code and modifying the corresponding configuration you can open a new task
        MagicianBlockchainScan.create()
                .setRpcUrl(
                        EthRpcInit.create()// Set multiple addresses, polling policy will be used automatically to do load balancing
                                .addRpcUrl("https://mainnet.infura.io/v3/04b7923be53e4534b0c97d11b529085d")
                                .addRpcUrl("https://mainnet.infura.io/v3/04b7923be53e4534b0c97d11b529085d")
                                .addRpcUrl("https://mainnet.infura.io/v3/04b7923be53e4534b0c97d11b529085d")
                )
                .setScanPeriod(5000)
                .setBeginBlockNumber(BigInteger.valueOf(19255387))
                .addEthMonitorEvent(new EventOne())

                .start();


        System.out.println( "Hello World!" );



    }





}
