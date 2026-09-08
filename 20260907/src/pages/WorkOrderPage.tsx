import { Button, Form, Input, Select } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { AgGridReact } from "ag-grid-react";
import { useRef, useState } from "react";
import { WorkOrderGrid } from "../components/WorkOrderGrid";
import { WorkOrderGroupPopup, type WorkOrderOption } from "../components/WorkOrderGroupPopup";

export interface WorkOrder {
  workNumber: string;
  factoryCode: string;
  lineCode: string;
  productGroupCode: string;
}

const factoryOptions: WorkOrderOption[] = [
  { label: "공장1", value: "fac1" },
  { label: "공장2", value: "fac2" },
];

const lineOptions: WorkOrderOption[] = [
  { label: "A라인", value: "line1a", parent: "fac1" },
  { label: "B라인", value: "line1b", parent: "fac1" },
  { label: "C라인", value: "line2c", parent: "fac2" },
  { label: "D라인", value: "line2d", parent: "fac2" },
];

const groupOptions: WorkOrderOption[] = [
  { label: "DRAM", value: "g-dram", parent: "line1a" },
  { label: "NAND", value: "g-nand", parent: "line1a" },
  { label: "Foundry", value: "g-foundry", parent: "line1b" },
  { label: "CIS", value: "g-cis", parent: "line1b" },
  { label: "OLED", value: "g-oled", parent: "line2c" },
  { label: "LCD", value: "g-lcd", parent: "line2c" },
  { label: "원통형", value: "g-cyl", parent: "line2d" },
  { label: "파우치형", value: "g-pouch", parent: "line2d" },
];

const ALL_WORK_ORDERS: WorkOrder[] = [
  { workNumber: "WO-2026-00001", factoryCode: "fac1", lineCode: "line1a", productGroupCode: "g-dram" },
  { workNumber: "WO-2026-00002", factoryCode: "fac1", lineCode: "line1a", productGroupCode: "g-nand" },
  { workNumber: "WO-2026-00003", factoryCode: "fac1", lineCode: "line1b", productGroupCode: "g-foundry" },
  { workNumber: "WO-2026-00004", factoryCode: "fac2", lineCode: "line2c", productGroupCode: "g-oled" },
  { workNumber: "WO-2026-00005", factoryCode: "fac1", lineCode: "line1b", productGroupCode: "g-cis" },
  { workNumber: "WO-2026-00006", factoryCode: "fac2", lineCode: "line2d", productGroupCode: "g-cyl" },
  { workNumber: "WO-2026-00007", factoryCode: "fac2", lineCode: "line2c", productGroupCode: "g-lcd" },
  { workNumber: "WO-2026-00008", factoryCode: "fac2", lineCode: "line2d", productGroupCode: "g-pouch" },
];

function optionLabel(options: WorkOrderOption[], value?: string) {
  return options.find((item) => item.value === value)?.label ?? "";
}

function groupsForFactory(factory?: string) {
  if (!factory) return groupOptions;
  const lines = new Set(lineOptions.filter((item) => item.parent === factory).map((item) => item.value));
  return groupOptions.filter((item) => item.parent && lines.has(item.parent));
}

export function WorkOrderPage() {
  const gridRef = useRef<AgGridReact<WorkOrder>>(null);
  const [form] = Form.useForm<{ workNumber?: string; factory?: string; productGroupCode?: string }>();
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [originals, setOriginals] = useState<WorkOrder[]>([]);
  const [searched, setSearched] = useState(false);
  const [searchPopupOpen, setSearchPopupOpen] = useState(false);
  const [popupRow, setPopupRow] = useState<WorkOrder | null>(null);

  const replaceRow = (next: WorkOrder) => {
    setWorkOrders((prev) => prev.map((row) => (row.workNumber === next.workNumber ? next : row)));
  };

  return (
    <>
      <Form
        form={form}
        onFinish={(values) => {
          const keyword = values.workNumber?.trim();
          const rows = ALL_WORK_ORDERS.filter((row) => {
            if (keyword && !row.workNumber.includes(keyword)) return false;
            if (values.factory && row.factoryCode !== values.factory) return false;
            if (values.productGroupCode && row.productGroupCode !== values.productGroupCode) return false;
            return true;
          }).map((row) => ({ ...row }));
          setWorkOrders(rows);
          setOriginals(rows.map((row) => ({ ...row })));
          setSearched(true);
        }}
      >
        <Form.Item name="workNumber" label="작업지시번호">
          <Input />
        </Form.Item>
        <Form.Item name="factory" label="공장">
          <Select
            allowClear
            options={factoryOptions}
            onChange={() => form.setFieldValue("productGroupCode", undefined)}
          />
        </Form.Item>
        <Form.Item label="제품군" shouldUpdate>
          {() => {
            const code = form.getFieldValue("productGroupCode") as string | undefined;
            return (
              <Input
                readOnly
                allowClear
                value={optionLabel(groupOptions, code)}
                placeholder="제품군 선택"
                suffix={<SearchOutlined />}
                onClick={() => setSearchPopupOpen(true)}
                onChange={() => form.setFieldValue("productGroupCode", undefined)}
              />
            );
          }}
        </Form.Item>
        <Form.Item name="productGroupCode" hidden>
          <Input />
        </Form.Item>
        <Button type="primary" htmlType="submit">
          조회
        </Button>
        <Button
          onClick={() => {
            const changed = workOrders.flatMap((row) => {
              const original = originals.find((item) => item.workNumber === row.workNumber);
              if (!original) return [];
              const fields = (["factoryCode", "lineCode", "productGroupCode"] as const).flatMap((field) => {
                if (original[field] === row[field]) return [];
                return [{ field, from: original[field], to: row[field] }];
              });
              if (!fields.length) return [];
              return [{ workNumber: row.workNumber, fields }];
            });
            console.log("수정된 작업지시", changed);
          }}
        >
          저장
        </Button>
      </Form>

      {searched ? (
        <WorkOrderGrid
          gridRef={gridRef}
          rowData={workOrders}
          factoryOptions={factoryOptions}
          lineOptions={lineOptions}
          groupOptions={groupOptions}
          onRowChange={replaceRow}
          onProductGroupClick={setPopupRow}
        />
      ) : (
        <div className="grid-empty">조회를 누르면 작업지시가 나타납니다</div>
      )}

      <WorkOrderGroupPopup
        open={searchPopupOpen}
        value={form.getFieldValue("productGroupCode")}
        options={groupsForFactory(form.getFieldValue("factory"))}
        onCancel={() => setSearchPopupOpen(false)}
        onSelect={(code) => {
          form.setFieldValue("productGroupCode", code);
          setSearchPopupOpen(false);
        }}
      />
      <WorkOrderGroupPopup
        open={Boolean(popupRow)}
        value={popupRow?.productGroupCode}
        options={groupOptions.filter((item) => item.parent === popupRow?.lineCode)}
        onCancel={() => setPopupRow(null)}
        onSelect={(code) => {
          if (!popupRow) return;
          const next = { ...popupRow, productGroupCode: code };
          const node = gridRef.current?.api.getRowNode(popupRow.workNumber);
          node?.setData(next);
          replaceRow(next);
          setPopupRow(null);
        }}
      />
    </>
  );
}
