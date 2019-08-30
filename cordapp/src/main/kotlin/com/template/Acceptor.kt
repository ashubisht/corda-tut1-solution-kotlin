package com.template

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.flows.ContractUpgradeFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.MAX_PAGE_SIZE
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.NetworkHostAndPort

fun main(args: Array<String>) {
    Acceptor().main(args)
}

class Acceptor {
    fun main(args: Array<String>) {
        // Args accept contract rules
        //List below used for future purpose
        val partyProxies = System.getProperty("nodeAddress").split(",").map {
            val nodeAddress = NetworkHostAndPort.parse(it)
            val client = CordaRPCClient(nodeAddress)
            client.start("user1", "test").proxy
        }

        val allStates = mutableMapOf<Class<out ContractState>, Class<out Contract>>(
                IOUState::class.java to TemplateContract::class.java
        )

        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)

        partyProxies.forEach { proxy ->
            allStates.forEach {
                proxy.vaultQueryByWithPagingSpec(it.key, generalCriteria,
                        PageSpecification(1, MAX_PAGE_SIZE)).states.forEach { stateAndRef ->
                    proxy.startFlowDynamic(ContractUpgradeFlow.Initiate::class.java, stateAndRef, it.value )
                }
            }
        }

        println("Upgrading contract flow ran. Will be running contract upgrade flow on this node in background")
        Thread.sleep(10000)

        partyProxies.forEach { proxy ->
            allStates.forEach{
                proxy.vaultQueryByWithPagingSpec(it.key, generalCriteria,
                        PageSpecification(1, MAX_PAGE_SIZE)).states
            }
        }

    }
}