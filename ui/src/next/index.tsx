import React, { useState } from 'react';
import styled, { createGlobalStyle } from 'styled-components';
import DamlLedger, { useParty } from '@daml/react';
import { createToken, httpBaseUrl, wsBaseUrl } from '../config';
import { Login } from './login';
import { Buyer } from './buyer';
import { Seller } from './seller';

interface User {
  party: string;
  token: string;
}

const TabList = styled.div`
  display: flex;
  flex-wrap: wrap;
`;

const Tab = styled.div`
  margin: 24px;
  padding: 16px;
  background: #fff;
  box-shadow: 0 0 32px rgba(0, 0, 0, 0.2);
  height: 600px;
  flex: 0 0 calc((100% - 24px * 6) / 3);
  overflow: auto;
`

const GlobalStyles = createGlobalStyle`
  body {
    font-size: 16px;
    background: #fafafa;
  }
  html {
    box-sizing: border-box;
  }
  *, *:before, *:after {
    box-sizing: inherit;
  }
`;

function DummyView() {
  const party = useParty();

  return (
    <h1>{party} has no UI yet...</h1>
  );
}

function getView(party: string) {
  switch (party) {
    case 'Buyer': return Buyer;
    case 'Seller': return Seller;
    default: return DummyView;
  }
}

function Ledger(props: { user: User }) {
  const View = getView(props.user.party)
  return (
    <DamlLedger
      party={props.user.party}
      token={props.user.token}
      httpBaseUrl={httpBaseUrl}
      wsBaseUrl={wsBaseUrl}
    >
      <View />
    </DamlLedger>
  )
}


function useUsers(): [User[], (party: string) => void] {
  const [users, setUsers] = useState<User[]>([]);

  function append(party: string) {
    setUsers(users => [...users, {party, token: createToken(party)}]);
  }

  return [users, append];
}

export function App() {
  const [users, append] = useUsers();

  return (
    <TabList>
      <GlobalStyles />
      {
        users.map(user => (
          <Tab key={user.party}>
            <Ledger user={user} />
          </Tab>
        ))
      }
      <Tab>
        <Login onLogin={append} />
      </Tab>
    </TabList>
  )
}
