import React from 'react';
import Ledger from '@daml/ledger';
import { ContractId } from '@daml/types';
import { useParty, useStreamQuery, useLedger } from '@daml/react';
import { QuoteRequest, QuoteRequestAccepted } from '@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/QuoteRequest';

export function acceptQuoteRequest(ledger: Ledger, id: ContractId<QuoteRequest>) {
  // use a random string as the workflow id instead of a user-defined one
  const workflowId = String(Math.random());

  ledger.exercise(
    QuoteRequest.QuoteRequest_Accept,
    id,
    { workflowId },
  );
}

export function Seller() {
  const party = useParty();
  const ledger = useLedger();
  const requests = useStreamQuery(QuoteRequest);
  const accepted = useStreamQuery(QuoteRequestAccepted);

  return (
    <div>
      <h1>You're logged in as a seller (party: {party})</h1>
      <div>
        <h2>Received quote requests from buyers</h2>
        <ul>
          {
            requests.contracts.map(relationship => (
              <li key={relationship.contractId}>
                <button onClick={() => acceptQuoteRequest(ledger, relationship.contractId)}>
                  Accept quote request from {relationship.payload.seller}
                </button>
              </li>
            ))
          }
        </ul>
      </div>
      <div>
        <h2>Accepted quote requests</h2>
        <ul>
          {
            accepted.contracts.map(relationship => (
              <li key={relationship.contractId}>
                Quote request from {relationship.payload.buyer} was accepted (workflow id: {relationship.payload.workflowId})
              </li>
            ))
          }
        </ul>
      </div>
    </div>
  );
}
