import React, { useState } from 'react';

const partyNames = 'Buyer,Seller,Warehouse1,Warehouse2,Supplier,TransportCompany1,TransportCompany2'.split(',');

export function Login(props: {onLogin: (party: string) => void}) {
  const [party, setParty] = useState<string | undefined>();

  const parties = partyNames.map(party => (
    <option key={party} value={party}>{party}</option>
  ));
  return (
    <div>
      <select onChange={e => setParty(e.target.value)}>
        <option value=''>Select a party...</option>
        {parties}
      </select>
      <button disabled={!party} onClick={() => props.onLogin(party!)}>Log in</button>
    </div>
  )
}
