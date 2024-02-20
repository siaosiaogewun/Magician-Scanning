package com.blockchain.scanning.monitor;

import com.blockchain.scanning.chain.model.TransactionModel;
import com.blockchain.scanning.monitor.filter.EthMonitorFilter;

/**
 * Ethereum listening events
 */
public interface EthMonitorEvent {

    /**
     * Monitor filter
     * 监控过滤器
     * When a qualified transaction is scanned, the call method will be triggered
     * 当扫描到符合条件的交易时，会触发call方法
     * @return
     */
    default EthMonitorFilter ethMonitorFilter(){
        return null;
    }

    /**
     * Filter the transaction data according to the above conditions, and execute the monitoring event
     * 根据以上条件过滤交易数据，并执行监听事件
     *
     * @param transactionModel
     */
    void call(TransactionModel transactionModel);
}
