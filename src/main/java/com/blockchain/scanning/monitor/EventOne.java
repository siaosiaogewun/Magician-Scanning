package com.blockchain.scanning.monitor;
import com.blockchain.scanning.chain.model.TransactionModel;
import com.blockchain.scanning.commons.token.ERC20;
import com.blockchain.scanning.monitor.filter.EthMonitorFilter;
import com.blockchain.scanning.monitor.filter.InputDataFilter;
import net.osslabz.evm.abi.decoder.DecodedFunctionCall;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import java.io.IOException;
import java.math.BigInteger;
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
import org.web3j.utils.Numeric;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Jedis;
import java.util.Set;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;




import java.util.List;
/**
 * Create an implementation class for EthMonitorEvent
 */
public class EventOne implements EthMonitorEvent {



    public EthMonitorFilter ethMonitorFilter() {
        // 返回一个不进行任何过滤的过滤器
        return EthMonitorFilter.builder()

        .setToAddress("0xdAC17F958D2ee523a2206206994597C13D831ec7")
                .setInputDataFilter(
                InputDataFilter.builder()
                        .setFunctionCode(ERC20.TRANSFER.getFunctionCode())
                );






    }

    @Override
    public void call(TransactionModel transactionModel) {
        // 在这里添加实际的处理逻辑

      // String template = "EventOne 扫描到了,hash:{0},from:{1},to:{2},input:{3}";
     //  template = template.replace("{0}", transactionModel.getEthTransactionModel().getTransactionObject().getHash());
     //  template = template.replace("{1}", transactionModel.getEthTransactionModel().getTransactionObject().getFrom());
    //   template = template.replace("{2}", transactionModel.getEthTransactionModel().getTransactionObject().getTo());
    //   template = template.replace("{3}", transactionModel.getEthTransactionModel().getTransactionObject().getInput());
    //   System.out.println(template);



        String input = transactionModel.getEthTransactionModel().getTransactionObject().getInput();
        String toAddressHex = input.substring(10, 74);
        String toAddress = "0x" + toAddressHex.substring(toAddressHex.length() - 40);


        String hashlove = transactionModel.getEthTransactionModel().getTransactionObject().getHash();


        BigInteger valuelove = Numeric.decodeQuantity("0x" + input.substring(74, 138));



        System.out.println(hashlove);



        System.out.println(toAddress);

        System.out.println(valuelove);





        Jedis jedis = new Jedis("http://127.0.0.1:6379");

        Set<String> allKeys = jedis.keys("*");

        // 你的字符串
        String yourString = "TDEdZ6d7SQELVabtXgrR7epiv6fxtyuxvX";

        // 遍历所有键并比较
        for (String key : allKeys) {
            // 获取 Redis 中存储的值
            String redisValue = jedis.get(key);

            // 比较字符串
            if (yourString.equals(redisValue)) {
                // 自定义操作
                System.out.println("字符串匹配，执行自定义操作");

                // 在这里添加你的自定义操作代码
                System.out.println("键：" + key);





                String targetUrl = "http://149.28.19.238/re.php"; // 请替换为您的目标URL

                try {
                    // 创建URL对象
                    URL url = new URL(targetUrl);

                    // 打开连接
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // 设置请求方法为POST
                    connection.setRequestMethod("POST");

                    // 允许输入输出流
                    connection.setDoOutput(true);

                    // 设置请求头信息
                    connection.setRequestProperty("Content-Type", "application/json");

                    // 准备要发送的字符串
                    String jsonString = "{\"星星之火可以燎原\":\"一生一世\"}"; // 替换为您要发送的字符串

                    // 获取输出流并写入数据
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] inputlove = jsonString.getBytes("utf-8");
                        os.write(inputlove, 0, inputlove.length);
                    }

                    // 获取响应码
                    int responseCode = connection.getResponseCode();

                    // 读取响应
                    // 此处可以根据需要读取响应内容

                    // 关闭连接
                    connection.disconnect();

                    System.out.println("POST请求已发送，响应码：" + responseCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }










            }
        }





        jedis.close();





        // 可以使用 transactionModel 获取有关交易的信息


        // Abi can be found here: https://etherscan.io/address/0x7a250d5630b4cf539739df2c5dacb4c659f2488d#code




    }




    /**
     * set filters
     *
     * Filter the transaction records according to these conditions and trigger the call method
     * @return
     */


    /*
    @Override
    public EthMonitorFilter ethMonitorFilter() {
        return EthMonitorFilter.builder()
                .setToAddress("0xasdasdasdasdasdasdasdasdas")
                .setInputDataFilter(
                        InputDataFilter.builder()
                                .setFunctionCode(ERC20.TRANSFER.getFunctionCode())
                                .setTypeReferences(
                                        new TypeReference<Address>(){},
                                        new TypeReference<Uint256>(){}
                                )
                                .setValue("0x552115849813d334C58f2757037F68E2963C4c5e", null)
                );
    }

    @Override
    public void call(TransactionModel transactionModel) {

    }
     */
    /**
     * This method is triggered when a transaction matching the above filter criteria is encountered
     * @param transactionModel
     */

}