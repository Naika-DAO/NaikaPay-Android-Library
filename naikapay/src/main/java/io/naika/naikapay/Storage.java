
// This file is an automatically generated Java binding. Do not modify as any
// change will likely be lost upon the next re-generation!

package io.naika.naikapay;

import org.ethereum.geth.*;
import java.util.*;



public class Storage {
	// ABI is the input ABI used to generate the binding from.
	public final static String ABI = "[{\"inputs\":[],\"name\":\"retrieve\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"number\",\"type\":\"uint256\"}],\"name\":\"store\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
	
	

	// Ethereum address where this contract is located at.
	public final Address address;

	// Ethereum transaction in which this contract was deployed (if known!).
	public final Transaction deployer;

	// Contract instance bound to a blockchain address.
	private final BoundContract contract;

	// Creates a new instance of Storage, bound to a specific deployed contract.
	public Storage(Address address, EthereumClient client) throws Exception {
		this(Geth.bindContract(address, ABI, client));
	}

	public Storage(BoundContract bindContract) {
		contract = bindContract;
		address = bindContract.getAddress();
		deployer = bindContract.getDeployer();
	}


	// retrieve is a free data retrieval call binding the contract method 0x2e64cec1.
	//
	// Solidity: function retrieve() view returns(uint256)
	public BigInt retrieve(CallOpts opts) throws Exception {
		Interfaces args = Geth.newInterfaces(0);
		

		Interfaces results = Geth.newInterfaces(1);
		Interface result0 = Geth.newInterface(); result0.setDefaultBigInt(); results.set(0, result0);
		

		if (opts == null) {
			opts = Geth.newCallOpts();
		}
		this.contract.call(opts, results, "retrieve", args);
		return results.get(0).getBigInt();
		
	}
	

	
	// store is a paid mutator transaction binding the contract method 0x6057361d.
	//
	// Solidity: function store(uint256 number) returns()
	public Transaction store(TransactOpts opts, BigInt number) throws Exception {
		Interfaces args = Geth.newInterfaces(1);
		Interface arg0 = Geth.newInterface();arg0.setBigInt(number);args.set(0,arg0);
		
		return this.contract.transact(opts, "store"	, args);
	}
	

    

    
}

