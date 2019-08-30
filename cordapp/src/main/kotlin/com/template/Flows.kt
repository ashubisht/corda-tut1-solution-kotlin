package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.contracts.Command
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class IOUFlow(private val iouValue: Int,
              private val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        println("This is an upgraded flow")
        println("Getting notary")
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        println("Outputting state")
        // We create the transaction components.
        val outputState = IOUState(iouValue, ourIdentity, otherParty)
        val command = Command(TemplateContractV2.Commands.Action(), ourIdentity.owningKey)
        println("Building tx")
        // We create a transaction builder and add the components.
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(outputState, TemplateContractV2.ID)
                .addCommand(command)
        println("Signing tx")
        // We sign the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        println("Running finality subFlow")
        // We finalise the transaction.
        subFlow(FinalityFlow(signedTx))
    }
}
