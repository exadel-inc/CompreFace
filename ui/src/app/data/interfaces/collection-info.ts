import { SubjectExampleResponseItem } from './collection';

export interface CollectionInfo {
  faces: SubjectExampleResponseItem[];
  page_number: number;
  page_side: number;
  total_elements: number;
  total_pages: number;
}
