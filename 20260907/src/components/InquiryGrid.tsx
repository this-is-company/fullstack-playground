import { AgGridReact } from "ag-grid-react";
import type {
  CellClickedEvent,
  CellValueChangedEvent,
  ColDef,
  GetRowIdParams,
  ICellRendererParams,
  RowClassParams,
  ValueFormatterParams,
} from "ag-grid-community";
import { Button, Empty, Pagination, Spin } from "antd";
import { FileExcelOutlined, SaveOutlined, SearchOutlined } from "@ant-design/icons";
import { useEffect, useMemo, useRef, useState } from "react";
import type { FieldKey, InquiryRow } from "../types";
import { FIELD_META } from "../types";
import { cascadeRow, formatCode, parentFieldOf } from "../mock/hierarchy";
import { SelectCellEditor } from "./SelectCellEditor";
import { ProductGroupLookupModal } from "./ProductGroupLookupModal";

import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-alpine.css";

interface Props {
  rows: InquiryRow[];
  total: number;
  page: number;
  pageSize: number;
  loading: boolean;
  searched: boolean;
  flashedId: string | null;
  dirtyIds: Set<string>;
  onPageChange: (page: number, pageSize: number) => void;
  onRowCommit: (row: InquiryRow, field: FieldKey, oldValue: string, newValue: string) => void;
  onSave: (selected: InquiryRow[]) => void;
  onExcelDownload: () => void;
  excelLoading?: boolean;
  productGroupEditor?: "popup" | "select";
  hint?: string;
}

function money(value: number) {
  return value.toLocaleString("ko-KR");
}

function ProductGroupCell(params: ICellRendererParams<InquiryRow>) {
  return (
    <span className="grid-lookup-cell">
      <span className="grid-lookup-value">{formatCode(params.value) || "-"}</span>
      <SearchOutlined className="grid-lookup-icon" />
    </span>
  );
}

export function InquiryGrid({
  rows,
  total,
  page,
  pageSize,
  loading,
  searched,
  flashedId,
  dirtyIds,
  onPageChange,
  onRowCommit,
  onSave,
  onExcelDownload,
  excelLoading,
  productGroupEditor = "popup",
  hint = "체크한 행만 저장합니다.",
}: Props) {
  const gridRef = useRef<AgGridReact<InquiryRow>>(null);
  const [lookupRow, setLookupRow] = useState<InquiryRow | null>(null);

  useEffect(() => {
    gridRef.current?.api?.redrawRows();
  }, [flashedId, dirtyIds]);

  const applyProductGroup = (row: InquiryRow, nextCode: string) => {
    const oldValue = row.productGroup ?? "";
    if (oldValue === nextCode) return;
    const next = cascadeRow({ ...row, productGroup: nextCode }, "productGroup");
    const node = gridRef.current?.api.getRowNode(row.id);
    node?.setData(next);
    node?.setSelected(true);
    onRowCommit(next, "productGroup", oldValue, nextCode);
    gridRef.current?.api.refreshCells({
      rowNodes: node ? [node] : undefined,
      columns: FIELD_META.map((meta) => meta.key),
      force: true,
    });
  };

  const columnDefs = useMemo<ColDef<InquiryRow>[]>(() => {
    const usePopup = productGroupEditor === "popup";
    const dimensionCols: ColDef<InquiryRow>[] = FIELD_META.map((meta) => {
      const isProductGroupPopup = usePopup && meta.key === "productGroup";
      return {
        field: meta.key,
        headerName: isProductGroupPopup ? "제품군(팝업)" : meta.label,
        width: isProductGroupPopup ? 160 : 140,
        minWidth: 120,
        editable: (params) => {
          if (isProductGroupPopup) return false;
          const parent = parentFieldOf(meta.key);
          if (!parent) return true;
          return Boolean(params.data?.[parent]);
        },
        cellEditor: isProductGroupPopup ? undefined : SelectCellEditor,
        cellEditorPopup: !isProductGroupPopup,
        cellRenderer: isProductGroupPopup ? ProductGroupCell : undefined,
        valueSetter: (params) => {
          const field = params.colDef.field as FieldKey;
          if (!params.data || params.oldValue === params.newValue) return false;
          params.data[field] = params.newValue as InquiryRow[FieldKey];
          return true;
        },
        valueFormatter: (params: ValueFormatterParams<InquiryRow>) => formatCode(params.value) || "-",
      };
    });

    return [
      {
        headerName: "",
        width: 48,
        minWidth: 48,
        maxWidth: 48,
        pinned: "left",
        lockPosition: true,
        checkboxSelection: true,
        headerCheckboxSelection: true,
        headerCheckboxSelectionFilteredOnly: true,
        sortable: false,
        resizable: false,
        suppressMenu: true,
        editable: false,
      },
      {
        field: "orderNo",
        headerName: "주문번호",
        width: 150,
        pinned: "left",
        editable: false,
      },
      ...dimensionCols,
      {
        field: "qty",
        headerName: "수량",
        width: 90,
        editable: false,
        valueFormatter: (p) => (p.value == null ? "" : money(p.value)),
      },
      {
        field: "amount",
        headerName: "금액",
        width: 120,
        editable: false,
        valueFormatter: (p) => (p.value == null ? "" : `${money(p.value)}원`),
      },
      { field: "orderDate", headerName: "주문일", width: 120, editable: false },
    ];
  }, [productGroupEditor]);

  const defaultColDef = useMemo<ColDef<InquiryRow>>(
    () => ({
      sortable: true,
      resizable: true,
      filter: true,
      suppressMovable: true,
    }),
    [],
  );

  const onCellValueChanged = (event: CellValueChangedEvent<InquiryRow>) => {
    const field = event.colDef.field as FieldKey;
    const oldValue = (event.oldValue as string) ?? "";
    const newValue = (event.newValue as string) ?? "";
    if (!event.data || oldValue === newValue) return;

    const next = cascadeRow({ ...event.data, [field]: newValue }, field);
    event.node.setData(next);
    event.node.setSelected(true);
    onRowCommit(next, field, oldValue, newValue);
    event.api.refreshCells({
      rowNodes: [event.node],
      columns: FIELD_META.map((meta) => meta.key),
      force: true,
    });
  };

  if (!searched) {
    return (
      <div className="grid-empty">
        <Empty description="조회조건을 선택한 뒤 [조회]를 누르면 그리드가 나타납니다" />
      </div>
    );
  }

  return (
    <div className="grid-wrap">
      <div className="grid-toolbar">
        <div className="grid-toolbar-meta">
          <span className="grid-total">
            전체 <b>{total.toLocaleString("ko-KR")}</b>건
          </span>
          <span className="grid-toolbar-split" aria-hidden>
            |
          </span>
          <span className="grid-toolbar-hint">{hint}</span>
        </div>
        <div className="grid-toolbar-actions">
          <Button
            type="primary"
            icon={<SaveOutlined />}
            onClick={() => onSave(gridRef.current?.api.getSelectedRows() ?? [])}
          >
            저장
          </Button>
          <span className="grid-toolbar-split" aria-hidden>
            |
          </span>
          <Button icon={<FileExcelOutlined />} loading={excelLoading} onClick={onExcelDownload}>
            엑셀다운로드
          </Button>
        </div>
      </div>
      <Spin spinning={loading} tip="mock API 호출 중...">
        <div className="ag-theme-alpine grid-surface">
          <AgGridReact<InquiryRow>
            ref={gridRef}
            rowData={rows}
            columnDefs={columnDefs}
            defaultColDef={defaultColDef}
            getRowId={(params: GetRowIdParams<InquiryRow>) => params.data.id}
            rowSelection="multiple"
            suppressRowClickSelection
            popupParent={document.body}
            singleClickEdit
            stopEditingWhenCellsLoseFocus={false}
            animateRows
            headerHeight={36}
            rowHeight={36}
            suppressPaginationPanel
            onCellValueChanged={onCellValueChanged}
            onCellClicked={(event: CellClickedEvent<InquiryRow>) => {
              if (productGroupEditor !== "popup") return;
              if (event.colDef.field !== "productGroup" || !event.data?.line) return;
              setLookupRow(event.data);
            }}
            overlayNoRowsTemplate="조회 결과가 없습니다"
            getRowClass={(params: RowClassParams<InquiryRow>) => {
              const classes = [];
              if (params.data?.id === flashedId) classes.push("row-updated");
              if (params.data && dirtyIds.has(params.data.id)) classes.push("row-dirty");
              return classes.join(" ");
            }}
          />
        </div>
      </Spin>
      <ProductGroupLookupModal
        open={productGroupEditor === "popup" && Boolean(lookupRow)}
        mode="single"
        query={{
          corp: lookupRow?.corp,
          division: lookupRow?.division,
          plant: lookupRow?.plant ? [lookupRow.plant] : [],
          line: lookupRow?.line ? [lookupRow.line] : [],
        }}
        value={lookupRow?.productGroup ? [lookupRow.productGroup] : []}
        hint={
          lookupRow
            ? `${formatCode(lookupRow.corp)} / ${formatCode(lookupRow.division)} / ${formatCode(lookupRow.plant)} / ${formatCode(lookupRow.line)} 조건으로 제품군을 조회합니다. 더블클릭하면 바로 선택됩니다.`
            : undefined
        }
        onCancel={() => setLookupRow(null)}
        onConfirm={(codes) => {
          if (lookupRow && codes[0]) applyProductGroup(lookupRow, codes[0]);
          setLookupRow(null);
        }}
      />
      <div className="grid-paging">
        <Pagination
          current={page}
          pageSize={pageSize}
          total={total}
          showSizeChanger
          pageSizeOptions={[10, 20, 50, 100]}
          showQuickJumper
          onChange={onPageChange}
        />
      </div>
    </div>
  );
}
