import { message } from "antd";
import { useCallback, useRef, useState } from "react";
import { InquiryGrid } from "./components/InquiryGrid";
import { SaveModal } from "./components/SaveModal";
import { SearchForm } from "./components/SearchForm";
import { fetchAllInquiries, fetchInquiries, getMasterCount, saveInquiries, updateMasterRow } from "./mock/api";
import { downloadExcel } from "./utils/excel";
import { childFieldsOf, codeName } from "./mock/hierarchy";
import type { FieldKey, InquiryRow, RowChange, SearchValues } from "./types";
import { EMPTY_SEARCH, FIELD_META } from "./types";

function fieldLabel(field: FieldKey) {
  return FIELD_META.find((item) => item.key === field)?.label ?? field;
}

function diffRow(current: InquiryRow, original: InquiryRow): RowChange["changes"] {
  return FIELD_META.flatMap((meta) => {
    if (current[meta.key] === original[meta.key]) return [];
    return [
      {
        field: meta.key,
        label: meta.label,
        from: codeName(original[meta.key]) || "(빈값)",
        to: codeName(current[meta.key]) || "(빈값)",
      },
    ];
  });
}

export default function App() {
  const [search, setSearch] = useState<SearchValues>({ ...EMPTY_SEARCH });
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [rows, setRows] = useState<InquiryRow[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [flashedId, setFlashedId] = useState<string | null>(null);
  const [dirtyIds, setDirtyIds] = useState<Set<string>>(new Set());
  const [saveOpen, setSaveOpen] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const [saveChanges, setSaveChanges] = useState<RowChange[]>([]);
  const [saveSkipped, setSaveSkipped] = useState(0);
  const [excelLoading, setExcelLoading] = useState(false);
  const flashTimer = useRef<number | null>(null);
  const originalsRef = useRef(new Map<string, InquiryRow>());

  const runFetch = useCallback(
    async (values: SearchValues, nextPage: number, nextPageSize: number, resetEdits = false) => {
      setLoading(true);
      const result = await fetchInquiries({ ...values, page: nextPage, pageSize: nextPageSize });
      if (resetEdits) {
        originalsRef.current.clear();
        setDirtyIds(new Set());
      }
      result.rows.forEach((row) => {
        if (!originalsRef.current.has(row.id)) {
          originalsRef.current.set(row.id, { ...row });
        }
      });
      setRows(result.rows);
      setTotal(result.total);
      setPage(result.page);
      setPageSize(result.pageSize);
      setSearched(true);
      setLoading(false);
    },
    [],
  );

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">사내 조회화면</p>
          <h1>생산실적 조회</h1>
        </div>
        <p className="header-note">mock {getMasterCount().toLocaleString("ko-KR")}건 · 서버 페이징</p>
      </header>

      <div className="app-body">
        <main className="app-main">
          <SearchForm
            value={search}
            loading={loading}
            onChange={(next) => setSearch(next)}
            onSearch={() => {
              void runFetch(search, 1, pageSize, true);
            }}
            onReset={() => {
              setSearch({ ...EMPTY_SEARCH });
              setSearched(false);
              setRows([]);
              setTotal(0);
              setPage(1);
              setDirtyIds(new Set());
              originalsRef.current.clear();
            }}
          />
          <InquiryGrid
            rows={rows}
            total={total}
            page={page}
            pageSize={pageSize}
            loading={loading}
            searched={searched}
            flashedId={flashedId}
            dirtyIds={dirtyIds}
            excelLoading={excelLoading}
            onPageChange={(nextPage, nextPageSize) => {
              void runFetch(search, nextPage, nextPageSize);
            }}
            onRowCommit={(row, field) => {
              updateMasterRow(row);
              setRows((prev) => prev.map((item) => (item.id === row.id ? row : item)));
              setDirtyIds((prev) => new Set(prev).add(row.id));
              setFlashedId(row.id);
              if (flashTimer.current) window.clearTimeout(flashTimer.current);
              flashTimer.current = window.setTimeout(() => setFlashedId(null), 1400);
              const cleared = childFieldsOf(field).filter((key) => !row[key]);
              message.success(
                cleared.length
                  ? `${fieldLabel(field)} 반영. ${cleared.map(fieldLabel).join(", ")} 값을 비웠습니다.`
                  : "그리드에 반영되었습니다",
              );
            }}
            onExcelDownload={() => {
              void (async () => {
                setExcelLoading(true);
                const allRows = await fetchAllInquiries(search);
                if (!allRows.length) {
                  setExcelLoading(false);
                  message.warning("다운로드할 데이터가 없습니다");
                  return;
                }
                const stamp = new Date().toISOString().slice(0, 10).replace(/-/g, "");
                downloadExcel(allRows, `생산실적_조회_${stamp}`);
                setExcelLoading(false);
                message.success(`전체 ${allRows.length.toLocaleString("ko-KR")}건을 엑셀로 받았습니다`);
              })();
            }}
            onSave={(selected) => {
              if (!selected.length) {
                message.warning("저장할 행을 체크하세요");
                return;
              }
              const changes: RowChange[] = [];
              let skipped = 0;
              selected.forEach((row) => {
                const original = originalsRef.current.get(row.id);
                const diffs = original ? diffRow(row, original) : [];
                if (!diffs.length) {
                  skipped += 1;
                  return;
                }
                changes.push({ id: row.id, orderNo: row.orderNo, changes: diffs });
              });
              if (!changes.length) {
                message.info("체크한 행에 수정된 값이 없습니다");
                return;
              }
              setSaveChanges(changes);
              setSaveSkipped(skipped);
              setSaveOpen(true);
            }}
          />
          <SaveModal
            open={saveOpen}
            loading={saveLoading}
            changes={saveChanges}
            skipped={saveSkipped}
            onCancel={() => setSaveOpen(false)}
            onConfirm={() => {
              void (async () => {
                setSaveLoading(true);
                const targets = saveChanges
                  .map((item) => rows.find((row) => row.id === item.id))
                  .filter((row): row is InquiryRow => Boolean(row));
                await saveInquiries(targets);
                targets.forEach((row) => originalsRef.current.set(row.id, { ...row }));
                setDirtyIds((prev) => {
                  const next = new Set(prev);
                  targets.forEach((row) => next.delete(row.id));
                  return next;
                });
                setSaveLoading(false);
                setSaveOpen(false);
                message.success(`${targets.length}건을 저장했습니다`);
              })();
            }}
          />
        </main>
      </div>
    </div>
  );
}
