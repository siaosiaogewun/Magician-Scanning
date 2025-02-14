package com.blockchain.scanning;

import com.blockchain.scanning.biz.thread.EventThreadPool;
import com.blockchain.scanning.chain.RetryStrategy;
import com.blockchain.scanning.biz.scan.ScanService;
import com.blockchain.scanning.commons.enums.ChainType;
import com.blockchain.scanning.commons.config.BlockChainConfig;
import com.blockchain.scanning.commons.config.rpcinit.RpcInit;
import com.blockchain.scanning.commons.config.rpcinit.impl.EthRpcInit;
import com.blockchain.scanning.commons.config.rpcinit.impl.SolRpcInit;
import com.blockchain.scanning.commons.config.rpcinit.impl.TronRpcInit;
import com.blockchain.scanning.monitor.EthMonitorEvent;
import com.blockchain.scanning.monitor.TronMonitorEvent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class, used to create a block sweep task
 *
 *创建扫描配置的主要函数都在里面了。
 */
public class MagicianBlockchainScan {

    /**
     * Business class, Used to perform scan block logic
     * 业务类，用于执行扫描块逻辑
     *
     */
    private ScanService scanService;

    /**
     * Configure the parameters required for this block scanning task
     * 配置本次块扫描任务所需的参数
     */
    private BlockChainConfig blockChainConfig;

    /**
     * Does the rpc address exist
     * rpc地址是否存在
     */
    private boolean rpcUrlExist = false;

    /**
     * all scan job
     */
    private static List<MagicianBlockchainScan> magicianBlockchainScans = new ArrayList<>();

    private MagicianBlockchainScan(){
        scanService = new ScanService();
        blockChainConfig = new BlockChainConfig();
    }

    public static MagicianBlockchainScan create(){
        return new MagicianBlockchainScan();
    }

    /**
     * Set the RPC URL for the blockchain
     * @param rpcInit
     * @return
     */
    public MagicianBlockchainScan setRpcUrl(RpcInit rpcInit){
        if(rpcInit instanceof EthRpcInit){
            blockChainConfig.setChainType(ChainType.ETH);
            blockChainConfig.setHttpService(rpcInit.getBlockChainConfig().getHttpService());
        } else if(rpcInit instanceof SolRpcInit){
            blockChainConfig.setChainType(ChainType.SOL);
            // TODO In development.......
        } else if(rpcInit instanceof TronRpcInit){
            blockChainConfig.setChainType(ChainType.TRON);
            blockChainConfig.setTronRpcUrls(rpcInit.getBlockChainConfig().getTronRpcUrls());
        }
        rpcUrlExist = true;
        return this;
    }

    /**
     * Setting the retry strategy
     * @param retryStrategy
     * @return
     */
    public MagicianBlockchainScan setRetryStrategy(RetryStrategy retryStrategy){
        blockChainConfig.setRetryStrategy(retryStrategy);
        return this;
    }

    /**
     * Set the scan polling interval, milliseconds
     * @param scanPeriod
     * @return
     */
    public MagicianBlockchainScan setScanPeriod(long scanPeriod) {
        blockChainConfig.setScanPeriod(scanPeriod);
        return this;
    }

    /**
     * Set the starting block height of the scan
     * @param beginBlockNumber
     * @return
     */
    public MagicianBlockchainScan setBeginBlockNumber(BigInteger beginBlockNumber) {
        blockChainConfig.setBeginBlockNumber(beginBlockNumber);
        return this;
    }

    /**
     * Set the end block height of the scan
     * @param endBlockNumber
     * @return
     */
    public MagicianBlockchainScan setEndBlockNumber(BigInteger endBlockNumber) {
        blockChainConfig.setEndBlockNumber(endBlockNumber);
        return this;
    }

    /**
     * Add ETH monitoring event
     * @param ethMonitorEvent
     * @return
     */
    public MagicianBlockchainScan addEthMonitorEvent(EthMonitorEvent ethMonitorEvent) {
        blockChainConfig.getEventConfig().addEthMonitorEvent(ethMonitorEvent);
        return this;
    }

    /**
     * Add TRON monitoring event
     * @param tronMonitorEvent
     * @return
     */
    public MagicianBlockchainScan addTronMonitorEvent(TronMonitorEvent tronMonitorEvent) {
        blockChainConfig.getEventConfig().addTronMonitorEvents(tronMonitorEvent);
        return this;
    }

    /**
     * start a task
     * @throws Exception
     */
    public void start() throws Exception {
        if (rpcUrlExist == false) {
            throw new Exception("rpcUrl cannot be empty");
        }

        if (blockChainConfig.getChainType() == null) {
            throw new Exception("ChainType cannot be empty");
        }

        if (blockChainConfig.getScanPeriod() < 1) {
            throw new Exception("scanPeriod must be greater than 1");
        }

        if(blockChainConfig.getDelayed() < 0){
            throw new Exception("delayed must be greater than 0");
        }

        if (blockChainConfig.getChainType().equals(ChainType.ETH)
                && (blockChainConfig.getEventConfig() == null
                || blockChainConfig.getEventConfig().getEthMonitorEvent() == null
                || blockChainConfig.getEventConfig().getEthMonitorEvent().size() < 1)
        ) {
            throw new Exception("You need to set up at least one monitor event");
        }

        if (blockChainConfig.getChainType().equals(ChainType.TRON)
                && (blockChainConfig.getEventConfig() == null
                || blockChainConfig.getEventConfig().getTronMonitorEvents() == null
                || blockChainConfig.getEventConfig().getTronMonitorEvents().size() < 1)
        ) {
            throw new Exception("You need to set up at least one monitor event");
        }

        // initialization scanService
        scanService.init(blockChainConfig);

        // execute the scan
        scanService.start();

        magicianBlockchainScans.add(this);
    }

    /**
     * Stop the current scan job
     */
    public void shutdown() {
        scanService.shutdown();
    }

    /**
     * Stop all scan job
     */
    public static void shutdownAll(){
        for(MagicianBlockchainScan magicianBlockchainScan : magicianBlockchainScans){
            magicianBlockchainScan.shutdown();
        }
        magicianBlockchainScans.clear();
        magicianBlockchainScans = null;

        EventThreadPool.shutdown();
    }

    /**
     * Get current block height
     * @return
     */
    public BigInteger getCurrentBlockHeight() {
        return scanService.getCurrentBlockHeight();
    }
}
