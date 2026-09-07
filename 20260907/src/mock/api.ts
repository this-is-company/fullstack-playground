import type { InquiryRow, PageResult, ProductGroupPage, ProductGroupQuery, SearchParams } from "../types";
import { generateRows } from "./generateData";
import { productGroupMasters } from "./hierarchy";

const ALL_ROWS = generateRows();

function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function matches(row: InquiryRow, params: SearchParams | Omit<SearchParams, "page" | "pageSize">): boolean {
  if (params.corp && row.corp !== params.corp) return false;
  if (params.division && row.division !== params.division) return false;
  if (params.plant.length && !params.plant.includes(row.plant)) return false;
  if (params.line.length && !params.line.includes(row.line)) return false;
  if (params.productGroup.length && !params.productGroup.includes(row.productGroup)) return false;
  if (params.status.length && !params.status.includes(row.status)) return false;
  if (params.customer.length && !params.customer.includes(row.customer)) return false;
  if (params.worker.length && !params.worker.includes(row.worker)) return false;
  if (params.equipment.length && !params.equipment.includes(row.equipment)) return false;
  if (params.warehouse.length && !params.warehouse.includes(row.warehouse)) return false;
  if (params.item.length && !params.item.includes(row.item)) return false;
  if (params.shipTo.length && !params.shipTo.includes(row.shipTo)) return false;
  if (params.inspectType.length && !params.inspectType.includes(row.inspectType)) return false;
  return true;
}

export async function fetchInquiries(params: SearchParams): Promise<PageResult> {
  const started = performance.now();
  await delay(380);
  const filtered = ALL_ROWS.filter((row) => matches(row, params));
  const start = (params.page - 1) * params.pageSize;
  const rows = filtered.slice(start, start + params.pageSize).map((row) => ({ ...row }));
  return {
    rows,
    total: filtered.length,
    page: params.page,
    pageSize: params.pageSize,
    elapsedMs: Math.round(performance.now() - started),
  };
}

export async function fetchAllInquiries(params: Omit<SearchParams, "page" | "pageSize">): Promise<InquiryRow[]> {
  await delay(380);
  return ALL_ROWS.filter((row) => matches(row, params)).map((row) => ({ ...row }));
}

export function updateMasterRow(next: InquiryRow) {
  const index = ALL_ROWS.findIndex((row) => row.id === next.id);
  if (index >= 0) {
    ALL_ROWS[index] = { ...next };
  }
}

export function getMasterCount() {
  return ALL_ROWS.length;
}

export async function saveInquiries(rows: InquiryRow[]) {
  await delay(380);
  rows.forEach(updateMasterRow);
  return { saved: rows.length };
}

function productGroupRequestUrl(query: ProductGroupQuery) {
  const params = new URLSearchParams();
  if (query.corp) params.set("corp", query.corp);
  if (query.division) params.set("division", query.division);
  query.plant.forEach((code) => params.append("plant", code));
  query.line.forEach((code) => params.append("line", code));
  if (query.keyword?.trim()) params.set("keyword", query.keyword.trim());
  return `/api/product-groups?${params.toString()}`;
}

export async function fetchProductGroups(query: ProductGroupQuery): Promise<ProductGroupPage> {
  const started = performance.now();
  await delay(420);
  const lineSet = new Set(query.line);
  const keyword = query.keyword?.trim().toLowerCase() ?? "";
  const rows = productGroupMasters.filter((row) => {
    if (lineSet.size && !lineSet.has(row.lineCode)) return false;
    if (!lineSet.size && query.plant.length && !query.plant.includes(row.plantCode)) return false;
    if (!lineSet.size && !query.plant.length && query.division && row.divisionCode !== query.division) return false;
    if (!lineSet.size && !query.plant.length && !query.division && query.corp && row.corpCode !== query.corp) return false;
    if (!keyword) return true;
    return (
      row.name.toLowerCase().includes(keyword) ||
      row.code.toLowerCase().includes(keyword) ||
      row.category.toLowerCase().includes(keyword) ||
      row.spec.toLowerCase().includes(keyword) ||
      row.process.toLowerCase().includes(keyword) ||
      row.owner.includes(query.keyword!.trim())
    );
  });
  return {
    rows,
    total: rows.length,
    elapsedMs: Math.round(performance.now() - started),
    requestUrl: productGroupRequestUrl(query),
  };
}
