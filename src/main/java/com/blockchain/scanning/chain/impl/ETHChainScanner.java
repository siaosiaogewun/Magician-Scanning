package com.blockchain.scanning.chain.impl;

import com.blockchain.scanning.biz.scan.ScanService;
import com.blockchain.scanning.biz.thread.EventQueue;
import com.blockchain.scanning.biz.thread.RetryStrategyQueue;
import com.blockchain.scanning.biz.thread.model.EventModel;
import com.blockchain.scanning.chain.ChainScanner;
import com.blockchain.scanning.chain.model.TransactionModel;
import com.blockchain.scanning.chain.model.eth.EthTransactionModel;
import com.blockchain.scanning.commons.codec.EthAbiCodec;
import com.blockchain.scanning.commons.enums.BlockEnums;
import com.blockchain.scanning.commons.util.StringUtil;
import com.blockchain.scanning.commons.config.BlockChainConfig;
import com.blockchain.scanning.monitor.EthMonitorEvent;
import com.blockchain.scanning.monitor.filter.EthMonitorFilter;
import com.blockchain.scanning.monitor.filter.InputDataFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Scan Ethereum, all chains that support the Ethereum standard (BSC, POLYGAN, etc.)
 */
public class ETHChainScanner extends ChainScanner {

    private Logger logger = LoggerFactory.getLogger(ETHChainScanner.class);

    /**
     * web3j
     */
    private List<Web3j> web3jList;

    /**
     * Get a list of Ethereum listening events
     */
    private List<EthMonitorEvent> ethMonitorEventList;

    /**
     * Initialize all member variables
     *
     * @param blockChainConfig
     * @param eventQueue
     */
    @Override
    public void init(BlockChainConfig blockChainConfig, EventQueue eventQueue, RetryStrategyQueue retryStrategyQueue, ScanService scanService) {
        super.init(blockChainConfig, eventQueue, retryStrategyQueue, scanService);

        this.ethMonitorEventList = blockChainConfig.getEventConfig().getEthMonitorEvent();

        this.web3jList = new ArrayList<>();
        for (HttpService httpService : blockChainConfig.getHttpService()) {
            this.web3jList.add(Web3j.build(httpService));
        }
    }

    /**
     * scan block
     *
     * @param beginBlockNumber
     */
    @Override
    public void scan(BigInteger beginBlockNumber) {
        try {
            Web3j web3j = this.web3jList.get(getNextIndex(web3jList.size()));

            BigInteger lastBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();

            if (beginBlockNumber.compareTo(BlockEnums.LAST_BLOCK_NUMBER.getValue()) == 0) {
                beginBlockNumber = lastBlockNumber;
            }

            if(scanService.getCurrentBlockHeight() == null){
                scanService.setCurrentBlockHeight(lastBlockNumber);
            }

            if (beginBlockNumber.compareTo(lastBlockNumber) > 0) {
                logger.info("[ETH], The block height on the chain has fallen behind the block scanning progress, pause scanning in progress ...... , scan progress [{}], latest block height on chain:[{}]", beginBlockNumber, lastBlockNumber);
                return;
            }

            if(blockChainConfig.getEndBlockNumber().compareTo(BigInteger.ZERO) > 0
                    && beginBlockNumber.compareTo(blockChainConfig.getEndBlockNumber()) >= 0){
                logger.info("[ETH], The current block height has reached the stop block height you set, so the scan job has been automatically stopped , scan progress [{}], end block height on chain:[{}]", beginBlockNumber, blockChainConfig.getEndBlockNumber());
                scanService.shutdown();
                return;
            }

            EthBlock block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(beginBlockNumber), true).send();
            if (block == null || block.getBlock() == null) {
                logger.info("[ETH], Block height [{}] does not exist", beginBlockNumber);
                if (lastBlockNumber.compareTo(beginBlockNumber) > 0) {
                    blockChainConfig.setBeginBlockNumber(beginBlockNumber.add(BigInteger.ONE));

                    //If the block is skipped, the retry strategy needs to be notified
                    addRetry(beginBlockNumber);
                }
                scanService.setCurrentBlockHeight(beginBlockNumber);
                return;
            }

            List<EthBlock.TransactionResult> transactionResultList = block.getBlock().getTransactions();
            if (transactionResultList == null || transactionResultList.size() < 1) {
                logger.info("[ETH], No transactions were scanned on block height [{}]", beginBlockNumber);
                if (lastBlockNumber.compareTo(beginBlockNumber) > 0) {
                    blockChainConfig.setBeginBlockNumber(beginBlockNumber.add(BigInteger.ONE));

                    //If the block is skipped, the retry strategy needs to be notified
                    addRetry(beginBlockNumber);
                }
                scanService.setCurrentBlockHeight(beginBlockNumber);
                return;
            }

            List<TransactionModel> transactionList = new ArrayList<>();

            for (EthBlock.TransactionResult<EthBlock.TransactionObject> transactionResult : transactionResultList) {

                EthBlock.TransactionObject transactionObject = transactionResult.get();

                transactionList.add(
                        TransactionModel.builder().setEthTransactionModel(
                                EthTransactionModel.builder()
                                        .setEthBlock(block)
                                        .setTransactionObject(transactionObject)
                        )
                );
            }

           // for (TransactionModel transactionModel : transactionList) {
                //System.out.println(transactionModel.toString());
            //}

            eventQueue.add(EventModel.builder()
                    .setCurrentBlockHeight(beginBlockNumber)
                    .setTransactionModels(transactionList)
            );

            blockChainConfig.setBeginBlockNumber(beginBlockNumber.add(BigInteger.ONE));

            scanService.setCurrentBlockHeight(beginBlockNumber);


            for (TransactionModel transactionModel : transactionList) {
                EthTransactionModel ethTransactionModel = transactionModel.getEthTransactionModel();
                EthBlock.TransactionObject transactionObject = ethTransactionModel.getTransactionObject();





                //System.out.println("Transaction Hash: " + transactionObject.getHash());
                //System.out.println("From Address: " + transactionObject.getFrom());
                //System.out.println("To Address: " + transactionObject.getTo());
                //System.out.println(" Value: " + transactionObject.getValue());

                //System.out.println(" Value: " + transactionObject.getInput());

                //System.out.println(" Value: " + transactionObject.getBlockHash());

                //System.out.println(" Value: " + transactionObject.getBlockNumberRaw());

                //System.out.println(" Value: " + transactionObject.getBlockHash());

                //System.out.println(" Value: " + transactionObject.getClass());

                //System.out.println(" Value: " + transactionObject.getAccessList());

                //System.out.println(" Value: " + transactionObject.getV());

                //System.out.println(" Value: " + transactionObject.getValue());

                //System.out.println(" Value: " + transactionObject.getValueRaw());

                //System.out.println(" Value: " + transactionObject.getType());

                //System.out.println(" Value: " + transactionObject.getTransactionIndex());

                //System.out.println(" Value: " + transactionObject.getTransactionIndexRaw());

                //System.out.println(" Value: " + transactionObject.getTo());

                //System.out.println(" Value: " + transactionObject.getS());

                //System.out.println(" Value: " + transactionObject.getRaw());

                //System.out.println(" Value: " + transactionObject.getR());

                //System.out.println(" Value: " + transactionObject.getPublicKey());

                //System.out.println(" Value: " + transactionObject.getNonce());

                //System.out.println(" Value: " + transactionObject.getNonceRaw());


               // System.out.println(" Value: " + transactionObject.getMaxPriorityFeePerGas());

                //System.out.println(" Value: " + transactionObject.getMaxPriorityFeePerGasRaw());

                //System.out.println(" Value: " + transactionObject.getMaxFeePerGas());


               // String input = transactionObject.getInput();
               // String toAddressHex = input.substring(10, 74);
               // String toAddress = "0x" + toAddressHex.substring(toAddressHex.length() - 40);


              //  System.out.println(toAddress);




                // 其他属性的输出...

               // System.out.println();  // 为了更好的可读性，添加空行分隔每一笔交易信息
            }




        } catch (Exception e) {
            logger.error("[ETH], An exception occurred while scanning, block height:[{}]", beginBlockNumber, e);
        }
    }

    /**
     * Process the scanned transaction data and perform monitoring events on demand
     *
     * @param transactionModel
     */
    @Override
    public void call(TransactionModel transactionModel) {
        EthBlock.TransactionObject transactionObject = transactionModel.getEthTransactionModel().getTransactionObject();

        for (EthMonitorEvent ethMonitorEvent : ethMonitorEventList) {
            try {
                if (transactionObject.getValue() == null) {
                    transactionObject.setValue("0");
                }

                if (transactionObject.getInput() != null
                        && transactionObject.getInput().toLowerCase().startsWith("0x") == false) {
                    transactionObject.setInput("0x" + transactionObject.getInput());
                }

                EthMonitorFilter ethMonitorFilter = ethMonitorEvent.ethMonitorFilter();

                if (ethMonitorFilter == null) {
                    ethMonitorEvent.call(transactionModel);
                    continue;
                }

                if (StringUtil.isNotEmpty(ethMonitorFilter.getFromAddress())
                        && (StringUtil.isEmpty(transactionObject.getFrom()) || ethMonitorFilter.getFromAddress().equals(transactionObject.getFrom().toLowerCase()) == false)) {
                    continue;
                }

                if (StringUtil.isNotEmpty(ethMonitorFilter.getToAddress())
                        && (StringUtil.isEmpty(transactionObject.getTo()) || ethMonitorFilter.getToAddress().equals(transactionObject.getTo().toLowerCase()) == false)) {
                    continue;
                }

                if (ethMonitorFilter.getMinValue() != null
                        && ethMonitorFilter.getMinValue().compareTo(transactionObject.getValue()) > 0) {
                    continue;
                }

                if (ethMonitorFilter.getMaxValue() != null
                        && ethMonitorFilter.getMaxValue().compareTo(transactionObject.getValue()) < 0) {
                    continue;
                }

                if (inputDataFilter(transactionObject, ethMonitorFilter) == false) {
                    continue;
                }

                ethMonitorEvent.call(transactionModel);
            } catch (Exception e) {
                logger.error("[ETH], An exception occurred in the call method of the listener", e);
            }
        }
    }

    /**
     * Filter by inputDataFilter
     *
     * @param transactionObject
     * @param ethMonitorFilter
     * @return
     */
    private boolean inputDataFilter(EthBlock.TransactionObject transactionObject, EthMonitorFilter ethMonitorFilter) {
        if (ethMonitorFilter.getInputDataFilter() == null) {
            return true;
        }

        if (StringUtil.isEmpty(transactionObject.getInput())
                || transactionObject.getInput().length() < 10) {
            return false;
        }

        InputDataFilter inputDataFilter = ethMonitorFilter.getInputDataFilter();

        if (StringUtil.isEmpty(inputDataFilter.getFunctionCode())) {
            return false;
        }

        if (transactionObject.getInput().toLowerCase().startsWith(inputDataFilter.getFunctionCode()) == false) {
            return false;
        }

        String inputData = "0x" + transactionObject.getInput().substring(10);

        if (inputDataFilter.getTypeReferences() != null
                && inputDataFilter.getTypeReferences().length >= 0
                && inputDataFilter.getValue() != null
                && inputDataFilter.getValue().length > 0) {

            if (inputDataFilter.getTypeReferences().length < inputDataFilter.getValue().length) {
                return false;
            }

            List<Type> result = EthAbiCodec.decoderInputData(inputData, inputDataFilter.getTypeReferences());

            if (result == null || result.size() < inputDataFilter.getValue().length) {
                return false;
            }

            for (int i = 0; i < inputDataFilter.getValue().length; i++) {
                String value = inputDataFilter.getValue()[i];
                Type paramValue = result.get(i);

                if (paramValue == null || paramValue.getValue() == null) {
                    return false;
                }

                if (value == null) {
                    continue;
                }

                if (value.equals(paramValue.getValue().toString().toLowerCase()) == false) {
                    return false;
                }
            }
        }

        return true;
    }
}
