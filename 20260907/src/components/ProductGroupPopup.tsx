import { Input, Tooltip } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { useState } from "react";
import type { ProductGroupQuery } from "../types";
import { formatSelectedLabel, formatSelectedTitle, isAllSelected } from "../utils/selectedLabel";
import { ProductGroupLookupModal } from "./ProductGroupLookupModal";

interface Props {
  disabled?: boolean;
  value: string[];
  allCodes: string[];
  query: ProductGroupQuery;
  onChange: (next: string[]) => void;
}

export function ProductGroupPopup({ disabled, value, allCodes, query, onChange }: Props) {
  const [open, setOpen] = useState(false);
  const allSelected = isAllSelected(value, allCodes);
  const display = formatSelectedLabel(value, allCodes, 2);
  const fullTitle = formatSelectedTitle(value, allCodes);

  const openModal = () => {
    if (disabled) return;
    setOpen(true);
  };

  return (
    <div className="multi-field">
      <Tooltip title={value.length ? fullTitle : undefined}>
        <Input
          readOnly
          disabled={disabled}
          value={display}
          placeholder={disabled ? "상위 조건을 먼저 선택하세요" : "제품군 선택 (API 팝업)"}
          onClick={openModal}
          suffix={<SearchOutlined onClick={openModal} />}
        />
      </Tooltip>
      <span className={`select-count${value.length ? "" : " is-empty"}`}>
        {allSelected ? "전체" : `${value.length}개`}
      </span>
      <ProductGroupLookupModal
        open={open}
        mode="multiple"
        query={query}
        value={value}
        hint={`라인 ${query.line.length}개 기준으로 mock API를 호출합니다. 키워드를 넣으면 같은 조건으로 다시 조회합니다.`}
        onCancel={() => setOpen(false)}
        onConfirm={(codes) => {
          onChange(codes);
          setOpen(false);
        }}
      />
    </div>
  );
}
