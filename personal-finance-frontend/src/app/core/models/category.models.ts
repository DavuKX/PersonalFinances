import { TransactionType } from './transaction.models';

export interface CategoryResponse {
  id: number;
  name: string;
  type: TransactionType;
  parentId: number | null;
  parentName: string | null;
  subcategories: CategoryResponse[];
}

export interface CreateCategoryRequest {
  name: string;
  type: TransactionType;
  parentId: number | null;
}

