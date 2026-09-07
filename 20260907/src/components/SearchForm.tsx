import { Button, Col, Divider, Row, Select, Space } from "antd";
import { DownOutlined, ReloadOutlined, SearchOutlined, UpOutlined } from "@ant-design/icons";
import { useState } from "react";
import type { FieldKey, FieldMeta, SearchValues } from "../types";
import { FIELD_META, modeLabel } from "../types";
import { childFieldsOf, getOptions, keepValidCodes } from "../mock/hierarchy";
import { isAllSelected } from "../utils/selectedLabel";
import { MultiSelectPopup } from "./MultiSelectPopup";
import { ProductGroupPopup } from "./ProductGroupPopup";

const BASIC_FIELDS = FIELD_META.slice(0, 6);
const DETAIL_FIELDS = FIELD_META.slice(6);

function countActiveDetails(value: SearchValues) {
  return DETAIL_FIELDS.reduce((total, meta) => {
    if (meta.mode === "single") return total + (value[meta.key] ? 1 : 0);
    return total + ((value[meta.key] as string[]).length > 0 ? 1 : 0);
  }, 0);
}

function cascadeSearch(next: SearchValues, changedField: FieldKey): SearchValues {
  const result = { ...next };
  for (const field of childFieldsOf(changedField)) {
    const meta = FIELD_META.find((item) => item.key === field);
    if (meta?.mode === "single") {
      const current = result[field] as string | undefined;
      const options = getOptions(field, result);
      if (current && !options.some((item) => item.code === current)) {
        (result[field] as string | undefined) = undefined;
      }
    } else {
      result[field] = keepValidCodes(field, result[field] as string[], result) as never;
    }
  }
  return result;
}

function fieldControl(meta: FieldMeta, value: SearchValues, apply: (field: FieldKey, nextValue: string | string[] | undefined) => void) {
  const optionItems = getOptions(meta.key, value);
  const options = optionItems.map((item) => ({
    value: item.code,
    label: item.name,
  }));
  const disabled = meta.key === "corp" ? false : optionItems.length === 0;
  const selected = meta.mode === "single" ? [] : (value[meta.key] as string[]);

  if (meta.key === "productGroup") {
    return (
      <ProductGroupPopup
        disabled={value.line.length === 0}
        value={selected}
        allCodes={optionItems.map((item) => item.code)}
        query={{
          corp: value.corp,
          division: value.division,
          plant: value.plant,
          line: value.line,
        }}
        onChange={(codes) => apply(meta.key, codes)}
      />
    );
  }

  if (meta.mode === "single") {
    return (
      <Select
        allowClear
        showSearch
        optionFilterProp="label"
        placeholder={`${meta.label} 선택`}
        disabled={disabled}
        value={value[meta.key] as string | undefined}
        options={options}
        onChange={(code) => apply(meta.key, code)}
      />
    );
  }

  if (meta.mode === "multiSelect") {
    const allSelected = isAllSelected(selected, options.map((item) => item.value));
    return (
      <div className="multi-field">
        <Select
          mode="multiple"
          allowClear
          showSearch
          maxTagCount={allSelected ? 1 : "responsive"}
          maxTagPlaceholder={allSelected ? () => "전체선택" : (omitted) => `외 ${omitted.length}개`}
          tagRender={(props) => {
            if (allSelected && props.value !== selected[0]) return <></>;
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
          placeholder={disabled ? "상위 조건을 먼저 선택하세요" : `${meta.label} 선택 (여러 개)`}
          disabled={disabled}
          value={selected}
          options={options}
          onChange={(codes) => apply(meta.key, codes)}
          dropdownRender={(menu) => (
            <>
              <div className="select-bulk">
                <Button
                  type="link"
                  size="small"
                  disabled={disabled || !options.length || allSelected}
                  onMouseDown={(e) => e.preventDefault()}
                  onClick={() => apply(meta.key, options.map((item) => item.value))}
                >
                  전체 선택 ({options.length})
                </Button>
                <Button type="link" size="small" disabled={!selected.length} onMouseDown={(e) => e.preventDefault()} onClick={() => apply(meta.key, [])}>
                  선택해제
                </Button>
                <span>
                  {allSelected ? "전체선택" : `${selected.length}/${options.length}`}
                </span>
              </div>
              <Divider style={{ margin: "4px 0" }} />
              {menu}
            </>
          )}
        />
        <span className={`select-count${selected.length ? "" : " is-empty"}`}>
          {allSelected ? "전체" : `${selected.length}개`}
        </span>
      </div>
    );
  }

  return (
    <MultiSelectPopup
      label={meta.label}
      disabled={disabled}
      options={optionItems}
      value={selected}
      onChange={(codes) => apply(meta.key, codes)}
    />
  );
}

interface Props {
  value: SearchValues;
  loading?: boolean;
  onChange: (next: SearchValues, changedField: FieldKey) => void;
  onSearch: () => void;
  onReset: () => void;
}

function renderFields(
  fields: FieldMeta[],
  value: SearchValues,
  apply: (field: FieldKey, nextValue: string | string[] | undefined) => void,
) {
  return fields.map((meta) => (
    <Col key={meta.key} xs={24} md={12} xl={8}>
      <div className="field-row" data-field={meta.key}>
        <label>
          {meta.label}
          <span className="field-mode">{meta.key === "productGroup" ? "API팝업" : modeLabel(meta.mode)}</span>
        </label>
        {fieldControl(meta, value, apply)}
      </div>
    </Col>
  ));
}

export function SearchForm({ value, loading, onChange, onSearch, onReset }: Props) {
  const [detailOpen, setDetailOpen] = useState(false);
  const apply = (field: FieldKey, nextValue: string | string[] | undefined) => {
    onChange(cascadeSearch({ ...value, [field]: nextValue } as SearchValues, field), field);
  };
  const detailCount = countActiveDetails(value);

  return (
    <div className="search-card">
      <div className="search-card-title">조회조건</div>
      <Row gutter={[16, 12]}>{renderFields(BASIC_FIELDS, value, apply)}</Row>
      {detailOpen && (
        <div className="detail-search">
          <div className="search-card-title">상세조건</div>
          <Row gutter={[16, 12]}>{renderFields(DETAIL_FIELDS, value, apply)}</Row>
        </div>
      )}
      <div className="search-actions">
        <Space>
          <Button type="primary" icon={<SearchOutlined />} loading={loading} onClick={onSearch}>
            조회
          </Button>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => {
              setDetailOpen(false);
              onReset();
            }}
          >
            초기화
          </Button>
          <Button icon={detailOpen ? <UpOutlined /> : <DownOutlined />} onClick={() => setDetailOpen((open) => !open)}>
            {detailOpen ? "상세닫기" : detailCount ? `상세 (${detailCount})` : "상세"}
          </Button>
        </Space>
      </div>
    </div>
  );
}
