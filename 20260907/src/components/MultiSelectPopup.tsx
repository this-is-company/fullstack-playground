import { Button, Checkbox, Empty, Input, Modal, Tooltip } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { useMemo, useState } from "react";
import type { CodeItem } from "../types";
import { formatCode } from "../mock/hierarchy";
import { formatSelectedLabel, formatSelectedTitle, isAllSelected } from "../utils/selectedLabel";

interface Props {
  label: string;
  disabled?: boolean;
  options: CodeItem[];
  value: string[];
  onChange: (next: string[]) => void;
}

export function MultiSelectPopup({ label, disabled, options, value, onChange }: Props) {
  const [open, setOpen] = useState(false);
  const [keyword, setKeyword] = useState("");
  const [draft, setDraft] = useState<string[]>(value);

  const filtered = useMemo(() => {
    const q = keyword.trim();
    if (!q) return options;
    return options.filter((item) => item.name.includes(q) || item.code.toLowerCase().includes(q.toLowerCase()));
  }, [keyword, options]);

  const optionCodes = options.map((item) => item.code);
  const allSelected = isAllSelected(value, optionCodes);
  const display = formatSelectedLabel(value, optionCodes, 2);
  const fullTitle = formatSelectedTitle(value, optionCodes);
  const draftAll = isAllSelected(draft, optionCodes);

  const openModal = () => {
    if (disabled) return;
    setKeyword("");
    setDraft(value);
    setOpen(true);
  };

  return (
    <div className="multi-field">
      <Tooltip title={value.length ? fullTitle : undefined}>
        <Input
          readOnly
          disabled={disabled}
          value={display}
          placeholder={disabled ? "상위 조건을 먼저 선택하세요" : `${label} 선택 (여러 개)`}
          onClick={openModal}
          suffix={<SearchOutlined onClick={openModal} />}
        />
      </Tooltip>
      <span className={`select-count${value.length ? "" : " is-empty"}`}>
        {allSelected ? "전체" : `${value.length}개`}
      </span>
      <Modal
        title={`${label} 다중 선택`}
        open={open}
        onCancel={() => setOpen(false)}
        width={520}
        footer={[
          <Button key="all" onClick={() => setDraft(options.map((item) => item.code))}>
            전체 선택 ({options.length})
          </Button>,
          <Button key="clear" onClick={() => setDraft([])}>
            선택해제
          </Button>,
          <Button key="cancel" onClick={() => setOpen(false)}>
            취소
          </Button>,
          <Button
            key="ok"
            type="primary"
            onClick={() => {
              onChange(draft);
              setOpen(false);
            }}
          >
            확인 ({draftAll ? "전체선택" : `${draft.length}개`})
          </Button>,
        ]}
      >
        <div className="popup-selected-summary">
          <strong>{draftAll ? `전체선택 (${draft.length}개)` : `선택한 값 ${draft.length}개`}</strong>
          <p>
            {draft.length
              ? draftAll
                ? "현재 목록의 항목을 모두 골랐습니다."
                : draft.map(formatCode).join(", ")
              : "아직 고른 항목이 없습니다."}
          </p>
        </div>
        <Input
          allowClear
          placeholder="이름 또는 코드 검색"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          style={{ marginBottom: 12 }}
        />
        <div className="popup-option-list">
          {filtered.length === 0 ? (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="선택 가능한 항목이 없습니다" />
          ) : (
            <Checkbox.Group value={draft} onChange={(next) => setDraft(next as string[])} style={{ width: "100%" }}>
              {filtered.map((item) => (
                <label key={item.code} className="popup-option">
                  <Checkbox value={item.code} />
                  <span className="popup-option-name">{item.name}</span>
                  <span className="popup-option-code">{item.code}</span>
                </label>
              ))}
            </Checkbox.Group>
          )}
        </div>
      </Modal>
    </div>
  );
}
