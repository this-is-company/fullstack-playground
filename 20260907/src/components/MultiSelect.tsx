import { Button, Divider, Select } from "antd";
import type { ReactNode } from "react";
import { isAllSelected } from "../utils/selectedLabel";

export interface MultiSelectOption {
  value: string;
  label: ReactNode;
  disabled?: boolean;
}

interface Props {
  label?: string;
  placeholder?: string;
  disabled?: boolean;
  options: MultiSelectOption[];
  value: string[];
  onChange: (next: string[]) => void;
}

export function MultiSelect({
  label,
  placeholder,
  disabled = false,
  options,
  value,
  onChange,
}: Props) {
  const optionValues = options.map((item) => String(item.value));
  const allSelected = isAllSelected(value, optionValues);

  return (
    <div className="multi-field">
      <Select
        mode="multiple"
        allowClear
        showSearch
        maxTagCount={allSelected ? 1 : "responsive"}
        maxTagPlaceholder={allSelected ? () => "전체선택" : (omitted) => `외 ${omitted.length}개`}
        tagRender={(props) => {
          if (allSelected && props.value !== value[0]) return <></>;
          return (
            <span className="ant-select-selection-item">
              <span className="ant-select-selection-item-content">{allSelected ? "전체선택" : props.label}</span>
              {allSelected ? null : (
                <span
                  className="ant-select-selection-item-remove"
                  onMouseDown={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                  }}
                  onClick={props.onClose}
                >
                  ×
                </span>
              )}
            </span>
          );
        }}
        optionFilterProp="label"
        placeholder={placeholder ?? (disabled ? "상위 조건을 먼저 선택하세요" : `${label ? `${label} 선택 (여러 개)` : "선택 (여러 개)"}`)}
        disabled={disabled}
        value={value}
        options={options}
        onChange={(codes) => onChange(codes as string[])}
        dropdownRender={(menu) => (
          <>
            <div className="select-bulk">
              <Button
                type="link"
                size="small"
                disabled={disabled || !options.length || allSelected}
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => onChange(optionValues)}
              >
                전체 선택 ({options.length})
              </Button>
              <Button
                type="link"
                size="small"
                disabled={!value.length}
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => onChange([])}
              >
                선택해제
              </Button>
              <span>
                {allSelected ? "전체선택" : `${value.length}/${options.length}`}
              </span>
            </div>
            <Divider style={{ margin: "4px 0" }} />
            {menu}
          </>
        )}
      />
      <span className={`select-count${value.length ? "" : " is-empty"}`}>
        {allSelected ? "전체" : `${value.length}개`}
      </span>
    </div>
  );
}
