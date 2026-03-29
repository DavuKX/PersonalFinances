import { TransactionType } from './transaction.models';

export interface CategoryResponse {
  id: string;
  name: string;
  transactionType: TransactionType;
  parentId: string | null;
  isDefault: boolean;
  createdAt: string;
}

export interface CreateCategoryRequest {
  name: string;
  transactionType: TransactionType;
  parentId: string | null;
}
