import { Button, Form, Input, Select } from "antd";
import { AgGridReact } from "ag-grid-react";

import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-alpine.css";
import {CellValueChangedEvent, ValueFormatterParams} from "ag-grid-community";
import {WorkOrderSelectEditor} from "../components/WorkOrderSelectEditor.tsx";

export interface WorkOrder {
  workNumber: string;
  factoryCode: string;
  factoryName: string;
}

const workOrders: WorkOrder[] = [
  { workNumber: "WO-2026-00001", factoryCode: "fac1", factoryName: "공장1" },
  { workNumber: "WO-2026-00002", factoryCode: "fac1", factoryName: "공장1" },
  { workNumber: "WO-2026-00003", factoryCode: "fac2", factoryName: "공장2" },
  { workNumber: "WO-2026-00004", factoryCode: "fac2", factoryName: "공장2" },
  { workNumber: "WO-2026-00005", factoryCode: "fac1", factoryName: "공장1" },
  { workNumber: "WO-2026-00006", factoryCode: "fac2", factoryName: "공장2" },
  { workNumber: "WO-2026-00007", factoryCode: "fac1", factoryName: "공장1" },
  { workNumber: "WO-2026-00008", factoryCode: "fac2", factoryName: "공장2" },
];

export function WorkOrderPage() {

  const factoryOptions = [
    { label: "공장1", value: "fac1" },
    { label: "공장2", value: "fac2" },
  ];
  return (
    <>
      <Form>
        <Form.Item name="workNumber" label="작업지시번호">
          <Input />
        </Form.Item>
        <Form.Item name="factory" label="공장">
          <Select options={factoryOptions} />
        </Form.Item>
        <Button type="primary">조회</Button>
      </Form>
      <section className="ag-theme-alpine grid-surface">
        <AgGridReact<WorkOrder>
          rowData={workOrders}
          columnDefs={[
            { field: "workNumber", headerName: "작업지시번호" },
            { field: "factoryCode", headerName: "공장",
              valueFormatter: (params: ValueFormatterParams<WorkOrder>) =>
                factoryOptions.find((item) => item.value === params.value)?.label ?? "",
              editable:true,
              cellEditor:WorkOrderSelectEditor,
              cellEditorParams: {
                options: factoryOptions,
              },
            },
          ]}
        />
      </section>
    </>
  );
}
