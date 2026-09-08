import type { Ref } from "react";
import { AgGridReact } from "ag-grid-react";
import type {
  CellClickedEvent,
  CellValueChangedEvent,
  ColDef,
  GetRowIdParams,
  ICellEditorParams,
  ICellRendererParams,
  ValueFormatterParams,
} from "ag-grid-community";
import { SearchOutlined } from "@ant-design/icons";
import type { WorkOrder } from "../pages/WorkOrderPage";
import { WorkOrderSelectEditor } from "./WorkOrderSelectEditor";
import type { WorkOrderOption } from "./WorkOrderGroupPopup";

import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-alpine.css";

interface Props {
  gridRef: Ref<AgGridReact<WorkOrder>>;
  rowData: WorkOrder[];
  factoryOptions: WorkOrderOption[];
  lineOptions: WorkOrderOption[];
  groupOptions: WorkOrderOption[];
  onRowChange: (next: WorkOrder, field: "factoryCode" | "lineCode" | "productGroupCode") => void;
  onProductGroupClick: (row: WorkOrder) => void;
}

function optionLabel(options: WorkOrderOption[], value: string | undefined) {
  return options.find((item) => item.value === value)?.label ?? "";
}

function ProductGroupCell(params: ICellRendererParams<WorkOrder>) {
  return (
    <span className="grid-lookup-cell">
      <span className="grid-lookup-value">{params.valueFormatted || params.value || "-"}</span>
      <SearchOutlined className="grid-lookup-icon" />
    </span>
  );
}

export function WorkOrderGrid({
  gridRef,
  rowData,
  factoryOptions,
  lineOptions,
  groupOptions,
  onRowChange,
  onProductGroupClick,
}: Props) {
  const columnDefs: ColDef<WorkOrder>[] = [
    { field: "workNumber", headerName: "작업지시번호", width: 160, editable: false },
    {
      field: "factoryCode",
      headerName: "공장",
      width: 150,
      editable: true,
      cellEditor: WorkOrderSelectEditor,
      cellEditorPopup: true,
      cellEditorParams: { options: factoryOptions },
      valueFormatter: (params: ValueFormatterParams<WorkOrder>) => optionLabel(factoryOptions, params.value),
    },
    {
      field: "lineCode",
      headerName: "라인",
      width: 140,
      editable: (params) => Boolean(params.data?.factoryCode),
      cellEditor: WorkOrderSelectEditor,
      cellEditorPopup: true,
      cellEditorParams: (params: ICellEditorParams<WorkOrder>) => ({
        options: lineOptions.filter((item) => item.parent === params.data?.factoryCode),
      }),
      valueFormatter: (params: ValueFormatterParams<WorkOrder>) => optionLabel(lineOptions, params.value),
    },
    {
      field: "productGroupCode",
      headerName: "제품군(팝업)",
      width: 170,
      editable: false,
      cellRenderer: ProductGroupCell,
      valueFormatter: (params: ValueFormatterParams<WorkOrder>) => optionLabel(groupOptions, params.value),
    },
  ];

  const applyCascade = (event: CellValueChangedEvent<WorkOrder>) => {
    const field = event.colDef.field;
    if (!event.data || (field !== "factoryCode" && field !== "lineCode")) return;
    if (event.oldValue === event.newValue) return;

    const next: WorkOrder = { ...event.data };
    if (field === "factoryCode") {
      const lineOk = lineOptions.some((item) => item.parent === next.factoryCode && item.value === next.lineCode);
      if (!lineOk) {
        next.lineCode = "";
        next.productGroupCode = "";
      }
    }
    if (field === "lineCode") {
      const groupOk = groupOptions.some((item) => item.parent === next.lineCode && item.value === next.productGroupCode);
      if (!groupOk) next.productGroupCode = "";
    }

    event.node.setData(next);
    event.api.refreshCells({
      rowNodes: [event.node],
      columns: ["factoryCode", "lineCode", "productGroupCode"],
      force: true,
    });
    onRowChange(next, field);
  };

  return (
    <div className="ag-theme-alpine grid-surface">
      <AgGridReact<WorkOrder>
        ref={gridRef}
        rowData={rowData}
        columnDefs={columnDefs}
        getRowId={(params: GetRowIdParams<WorkOrder>) => params.data.workNumber}
        popupParent={document.body}
        singleClickEdit
        stopEditingWhenCellsLoseFocus={false}
        animateRows
        headerHeight={36}
        rowHeight={36}
        onCellValueChanged={applyCascade}
        onCellClicked={(event: CellClickedEvent<WorkOrder>) => {
          if (event.colDef.field !== "productGroupCode" || !event.data?.lineCode) return;
          onProductGroupClick(event.data);
        }}
      />
    </div>
  );
}
