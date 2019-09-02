package com.template

import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.identity.Party

// ************
// * Contract *
// ************
class TemplateContractV2 : Contract, UpgradedContractWithLegacyConstraint<IOUState, IOUState> {
    override val legacyContract: ContractClassName = TemplateContract.ID
    override val legacyContractConstraint: AttachmentConstraint
        get() = HashAttachmentConstraint(SecureHash.parse("5c8bf104e14b8806bbd14c96e0dc572e0ea27c57341114777eb52d13967bb369"))

    override fun upgrade(state: IOUState) = IOUState(state.value, state.lender, state.borrower)

    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.TemplateContractV2"
    }
    
    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) = requireThat {
        "The value of transaction should be greater than 10" using (tx.outputsOfType<IOUState>().single().value >=10)
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Action : Commands
    }
}
