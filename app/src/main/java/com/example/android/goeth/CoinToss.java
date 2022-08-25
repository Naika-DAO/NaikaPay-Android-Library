package com.example.android.goeth;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.4.1.
 */
@SuppressWarnings("rawtypes")
public class CoinToss extends Contract {
    public static final String ABI = "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"inputs\":[],\"name\":\"buyChance\",\"outputs\":[],\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"claim\",\"outputs\":[],\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"deposit\",\"outputs\":[],\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getBalance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"internalType\":\"address payable\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"withdraw\",\"outputs\":[],\"stateMutability\":\"payable\",\"type\":\"function\"}]";

    public static final String BINARY = "608060405266038d7ea4c6800060015534801561001b57600080fd5b50600080546001600160a01b031916331790556102588061003d6000396000f3fe6080604052600436106100555760003560e01c806312065fe01461005a5780631734539f146100825780633ccfd60b1461008c5780634e71d92d146100945780638da5cb5b1461009c578063d0e30db01461008a575b600080fd5b34801561006657600080fd5b5061006f6100d4565b6040519081526020015b60405180910390f35b61008a6100f1565b005b61008a610165565b61008a6101b9565b3480156100a857600080fd5b506000546100bc906001600160a01b031681565b6040516001600160a01b039091168152602001610079565b600080546001600160a01b031633146100ec57600080fd5b504790565b60015434146101635760405162461bcd60e51b815260206004820152603460248201527f596f75206e65656420746f207370656e642065786163746c7920302e3030312060448201527345544820746f206275792061206368616e63652160601b606482015260840160405180910390fd5b565b6000546001600160a01b0316331461017c57600080fd5b600080546040516001600160a01b03909116914780156108fc02929091818181858888f193505050501580156101b6573d6000803e3d6000fd5b50565b60015433906108fc906101cd9060036101f5565b6040518115909202916000818181858888f193505050501580156101b6573d6000803e3d6000fd5b600081600019048311821515161561021d57634e487b7160e01b600052601160045260246000fd5b50029056fea26469706673582212201a2e9bacf9e160d972d724a4f318c34ed812e81e19533e3703546e175e8bb87464736f6c634300080d0033";

    public static final String FUNC_BUYCHANCE = "buyChance";

    public static final String FUNC_CLAIM = "claim";

    public static final String FUNC_DEPOSIT = "deposit";

    public static final String FUNC_GETBALANCE = "getBalance";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_WITHDRAW = "withdraw";

    @Deprecated
    protected CoinToss(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CoinToss(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected CoinToss(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected CoinToss(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> buyChance() {
        final Function function = new Function(
                FUNC_BUYCHANCE,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> claim() {
        final Function function = new Function(
                FUNC_CLAIM,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> deposit() {
        final Function function = new Function(
                FUNC_DEPOSIT,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> getBalance() {
        final Function function = new Function(FUNC_GETBALANCE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> withdraw() {
        final Function function = new Function(
                FUNC_WITHDRAW,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static CoinToss load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CoinToss(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static CoinToss load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new CoinToss(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static CoinToss load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new CoinToss(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static CoinToss load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new CoinToss(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<CoinToss> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CoinToss.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<CoinToss> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CoinToss.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<CoinToss> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CoinToss.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<CoinToss> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CoinToss.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}
