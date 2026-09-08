import { Empty, Input, Modal } from "antd";
import { useMemo, useState } from "react";

export interface WorkOrderOption {
  label: string;
  value: string;
  parent?: string;
}

interface Props {
  open: boolean;
  value?: string;
  options: WorkOrderOption[];
  onCancel: () => void;
  onSelect: (code: string) => void;
}

export function WorkOrderGroupPopup({ open, value, options, onCancel, onSelect }: Props) {
  const [keyword, setKeyword] = useState("");
  const filtered = useMemo(() => {
    const q = keyword.trim();
    if (!q) return options;
    return options.filter((item) => item.label.includes(q) || item.value.toLowerCase().includes(q.toLowerCase()));
  }, [keyword, options]);

  return (
    <Modal
      title="제품군 선택"
      open={open}
      onCancel={onCancel}
      footer={null}
      width={480}
      afterOpenChange={(next) => {
        if (next) setKeyword("");
      }}
    >
      <Input
        allowClear
        placeholder="이름 또는 코드 검색"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        style={{ marginBottom: 12 }}
      />
      <div className="popup-option-list">
        {filtered.length === 0 ? (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="선택 가능한 제품군이 없습니다" />
        ) : (
          filtered.map((item) => (
            <button
              key={item.value}
              type="button"
              className={`popup-option${item.value === value ? " is-current" : ""}`}
              onClick={() => onSelect(item.value)}
            >
              <span className="popup-option-name">{item.label}</span>
              <span className="popup-option-code">{item.value}</span>
            </button>
          ))
        )}
      </div>
    </Modal>
  );
}
