import Ledger from '@daml/ledger';
import { ContractId } from '@daml/types';
import { BuyerSellerRelationship } from '@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/Relationship';
import { sendQuoteRequest } from './buyer';

// utility to quickly create a jest.Mocked<T>
function mock<T>(m: Partial<T>) {
  return m as unknown as jest.Mocked<T>;
}

describe('<Buyer />', () => {
  describe('sendQuoteRequest', () => {
    it('exercises the right choice', () => {
      const ledger = mock<Ledger>({
        exercise: jest.fn(),
      });
      sendQuoteRequest(ledger, 'seller' as ContractId<BuyerSellerRelationship>);

      expect(ledger.exercise).toHaveBeenCalledWith(
        BuyerSellerRelationship.BuyerSellerRelationship_SendQuoteRequest,
        'seller',
        {
          products: [{
            productName: 'Spice',
            quantity: '1000',
            deliveryFrom: '2021-08-01',
            deliveryTo: '2021-09-01',
          }],
        },
      );
    });
  });
});
