import React from 'react';
import Ledger from '@daml/ledger';
import { ContractId } from '@daml/types';
import { useStreamQuery, useParty, useLedger } from '@daml/react';
import { BuyerSellerRelationship } from '@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/Relationship';
import { QuoteRequest } from '@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/QuoteRequest';

// We'll send a hardcoded product list in the quote request
const quoteRequest = {
  products: [{
    productName: 'Spice',
    quantity: '1000',
    deliveryFrom: '2021-08-01',
    deliveryTo: '2021-09-01',
  }],
};

export function sendQuoteRequest(ledger: Ledger, seller: ContractId<BuyerSellerRelationship>) {
  ledger.exercise(
    BuyerSellerRelationship.BuyerSellerRelationship_SendQuoteRequest,
    seller,
    quoteRequest,
  );
}

export function Buyer() {
  const party = useParty();
  const ledger = useLedger();
  const sellers = useStreamQuery(BuyerSellerRelationship);
  const requests = useStreamQuery(QuoteRequest);
  return (
    <div>
      <h1>You're logged in as a buyer (party: {party})</h1>
      <div>
        <h2>Available sellers</h2>
        <ul>
          {
            sellers.contracts.map(relationship => (
              <li key={relationship.contractId}>
                <button onClick={() => sendQuoteRequest(ledger, relationship.contractId)}>
                  Send quote request to {relationship.payload.seller}
                </button>
              </li>
            ))
          }
        </ul>
      </div>
      <div>
        <h2>Submitted quote requests to sellers</h2>
        <ul>
          {
            requests.contracts.map(relationship => (
              <li key={relationship.contractId}>
                Quote request was submitted to {relationship.payload.seller}
              </li>
            ))
          }
        </ul>
      </div>
    </div>
  );
}
