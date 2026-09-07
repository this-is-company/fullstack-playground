import { useMemo, useState } from "react";
import type { CustomCellEditorProps } from "ag-grid-react";
import type { FieldKey, InquiryRow } from "../types";
import { getOptions } from "../mock/hierarchy";

export function SelectCellEditor(props: CustomCellEditorProps<InquiryRow, string>) {
  const field = props.colDef.field as FieldKey;
  const [keyword, setKeyword] = useState("");

  const options = useMemo(() => getOptions(field, props.data), [field, props.data]);
  const filtered = options.filter(
    (item) =>
      !keyword.trim() ||
      item.name.includes(keyword.trim()) ||
      item.code.toLowerCase().includes(keyword.trim().toLowerCase()),
  );

  const choose = (code: string) => {
    props.onValueChange(code);
    props.stopEditing();
  };

  return (
    <div
      className="grid-select-editor"
      onMouseDown={(e) => {
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
      }}
    >
      <input
        autoFocus
        className="grid-select-filter"
        placeholder="검색"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === "Enter" && filtered[0]) {
            e.preventDefault();
            choose(filtered[0].code);
          }
        }}
      />
      <ul className="grid-select-list">
        {filtered.map((item) => (
          <li key={item.code}>
            <button
              type="button"
              className={item.code === props.value ? "is-current" : ""}
              onMouseDown={(e) => {
                e.preventDefault();
                e.stopPropagation();
                e.nativeEvent.stopImmediatePropagation();
                choose(item.code);
              }}
            >
              {item.name}
            </button>
          </li>
        ))}
        {filtered.length === 0 && <li className="grid-select-empty">선택 가능한 값이 없습니다</li>}
      </ul>
    </div>
  );
}
