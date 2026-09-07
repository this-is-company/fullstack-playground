import { Select } from "antd";
import type { CustomCellEditorProps } from "ag-grid-react";
import type { WorkOrder } from "../pages/WorkOrderPage";

interface SelectOption {
  label: string;
  value: string;
}

interface WorkOrderSelectEditorProps extends CustomCellEditorProps<WorkOrder, string> {
  options: SelectOption[];
}

export function WorkOrderSelectEditor(props: WorkOrderSelectEditorProps) {
  return (
    <Select
      autoFocus
      style={{ width: 180 }}
      options={props.options}
      value={props.value}
      onChange={(code) => {
        props.onValueChange(code);
        props.stopEditing();
      }}
    />
  );
}
