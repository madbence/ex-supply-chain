import Ledger from '@daml/ledger';
import { ContractId } from '@daml/types';
import { QuoteRequest } from '@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/QuoteRequest';
import { acceptQuoteRequest } from './seller';

function mock<T>(m: Partial<T>) {
  return m as unknown as jest.Mocked<T>;
}

describe('<Seller />', () => {
  describe('acceptQuoteRequest', () => {
    it('exercises the right choice', () => {
      const ledger = mock<Ledger>({
        exercise: jest.fn(),
      });
      acceptQuoteRequest(ledger, 'quote-request-id' as ContractId<QuoteRequest>);

      expect(ledger.exercise).toHaveBeenCalledWith(
        QuoteRequest.QuoteRequest_Accept,
        'quote-request-id',
        {
          workflowId: expect.any(String),
        },
      );
    });
  });
});
