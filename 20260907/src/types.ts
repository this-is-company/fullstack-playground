export const FIELD_ORDER = [
  "corp",
  "division",
  "plant",
  "line",
  "productGroup",
  "status",
  "customer",
  "worker",
  "equipment",
  "warehouse",
  "item",
  "shipTo",
  "inspectType",
] as const;

export type FieldKey = (typeof FIELD_ORDER)[number];

export type SelectMode = "single" | "multiPopup" | "multiSelect";

export interface FieldMeta {
  key: FieldKey;
  label: string;
  mode: SelectMode;
  parent: FieldKey | null;
}

export const FIELD_META: FieldMeta[] = [
  { key: "corp", label: "법인", mode: "single", parent: null },
  { key: "division", label: "사업부", mode: "single", parent: "corp" },
  { key: "plant", label: "공장", mode: "multiPopup", parent: "division" },
  { key: "line", label: "라인", mode: "multiPopup", parent: "plant" },
  { key: "productGroup", label: "제품군", mode: "multiPopup", parent: "line" },
  { key: "status", label: "상태", mode: "multiPopup", parent: "productGroup" },
  { key: "customer", label: "고객사", mode: "multiSelect", parent: "corp" },
  { key: "worker", label: "작업자", mode: "multiSelect", parent: "customer" },
  { key: "equipment", label: "설비", mode: "multiPopup", parent: "plant" },
  { key: "warehouse", label: "창고", mode: "multiSelect", parent: "corp" },
  { key: "item", label: "품목", mode: "multiPopup", parent: "productGroup" },
  { key: "shipTo", label: "출하처", mode: "multiSelect", parent: "customer" },
  { key: "inspectType", label: "검사유형", mode: "multiPopup", parent: "corp" },
];

export function modeLabel(mode: SelectMode) {
  if (mode === "single") return "단일";
  if (mode === "multiPopup") return "팝업";
  return "멀티";
}

export function isSingleField(key: FieldKey) {
  return FIELD_META.find((item) => item.key === key)?.mode === "single";
}

export interface CodeItem {
  code: string;
  name: string;
  parentCode: string | null;
}

export interface ProductGroupMaster {
  code: string;
  name: string;
  parentCode: string;
  category: string;
  spec: string;
  unit: string;
  yieldRate: number;
  leadTimeDay: number;
  process: string;
  owner: string;
  revision: string;
  monthlyCapacity: number;
  lineCode: string;
  lineName: string;
  plantCode: string;
  plantName: string;
  divisionCode: string;
  divisionName: string;
  corpCode: string;
  corpName: string;
}

export interface ProductGroupQuery {
  corp?: string;
  division?: string;
  plant: string[];
  line: string[];
  keyword?: string;
}

export interface ProductGroupPage {
  rows: ProductGroupMaster[];
  total: number;
  elapsedMs: number;
  requestUrl: string;
}

export interface InquiryRow {
  id: string;
  corp: string;
  division: string;
  plant: string;
  line: string;
  productGroup: string;
  status: string;
  customer: string;
  worker: string;
  equipment: string;
  warehouse: string;
  item: string;
  shipTo: string;
  inspectType: string;
  orderNo: string;
  qty: number;
  amount: number;
  orderDate: string;
}

export interface SearchValues {
  corp: string | undefined;
  division: string | undefined;
  plant: string[];
  line: string[];
  productGroup: string[];
  status: string[];
  customer: string[];
  worker: string[];
  equipment: string[];
  warehouse: string[];
  item: string[];
  shipTo: string[];
  inspectType: string[];
}

export interface SearchParams extends SearchValues {
  page: number;
  pageSize: number;
}

export interface PageResult {
  rows: InquiryRow[];
  total: number;
  page: number;
  pageSize: number;
  elapsedMs: number;
}

export interface FieldChange {
  field: FieldKey;
  label: string;
  from: string;
  to: string;
}

export interface RowChange {
  id: string;
  orderNo: string;
  changes: FieldChange[];
}

export const EMPTY_SEARCH: SearchValues = {
  corp: undefined,
  division: undefined,
  plant: [],
  line: [],
  productGroup: [],
  status: [],
  customer: [],
  worker: [],
  equipment: [],
  warehouse: [],
  item: [],
  shipTo: [],
  inspectType: [],
};
