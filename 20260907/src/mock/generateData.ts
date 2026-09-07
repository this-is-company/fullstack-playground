import type { InquiryRow } from "../types";
import { getOptions, validPaths } from "./hierarchy";

function mulberry32(seed: number) {
  return function random() {
    let t = (seed += 0x6d2b79f5);
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

export const TOTAL_ROWS = 6000;

export function generateRows(count = TOTAL_ROWS, seed = 20260907): InquiryRow[] {
  const rand = mulberry32(seed);
  const pick = <T,>(list: T[]) => list[Math.floor(rand() * list.length)];

  return Array.from({ length: count }, (_, index) => {
    const path = pick(validPaths);
    const customers = getOptions("customer", path);
    const customer = pick(customers).code;
    const withCustomer = { ...path, customer };
    const qty = 10 + Math.floor(rand() * 990);
    const unit = 1000 + Math.floor(rand() * 9000);
    const day = 1 + Math.floor(rand() * 28);
    const month = 1 + Math.floor(rand() * 9);
    return {
      id: `ROW-${String(index + 1).padStart(5, "0")}`,
      ...path,
      customer,
      worker: pick(getOptions("worker", withCustomer)).code,
      equipment: pick(getOptions("equipment", path)).code,
      warehouse: pick(getOptions("warehouse", path)).code,
      item: pick(getOptions("item", path)).code,
      shipTo: pick(getOptions("shipTo", withCustomer)).code,
      inspectType: pick(getOptions("inspectType", path)).code,
      orderNo: `ORD-2026-${String(index + 1).padStart(5, "0")}`,
      qty,
      amount: qty * unit,
      orderDate: `2026-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`,
    };
  });
}
