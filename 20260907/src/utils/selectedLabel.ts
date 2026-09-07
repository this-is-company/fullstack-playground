import { formatCode } from "../mock/hierarchy";

export function isAllSelected(selected: string[], optionCodes: string[]) {
  if (!optionCodes.length || selected.length !== optionCodes.length) return false;
  const picked = new Set(selected);
  return optionCodes.every((code) => picked.has(code));
}

export function formatSelectedLabel(codes: string[], optionCodes: string[] = [], visible = 2) {
  if (!codes.length) return "";
  if (isAllSelected(codes, optionCodes)) return "전체선택";
  const names = codes.map((code) => formatCode(code));
  if (names.length <= visible) return names.join(", ");
  return `${names.slice(0, visible).join(", ")} 외 ${names.length - visible}개`;
}

export function formatSelectedTitle(codes: string[], optionCodes: string[] = []) {
  if (isAllSelected(codes, optionCodes)) return `전체선택 (${codes.length}개)`;
  return codes.map((code) => formatCode(code)).join(", ");
}
