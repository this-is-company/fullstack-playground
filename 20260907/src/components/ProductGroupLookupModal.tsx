import { Button, Empty, Input, Modal, Spin, Table } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { useEffect, useState } from "react";
import type { ProductGroupMaster, ProductGroupQuery } from "../types";
import { fetchProductGroups } from "../mock/api";
import { isAllSelected } from "../utils/selectedLabel";

const COLUMNS = [
  { title: "코드", dataIndex: "code", width: 100 },
  { title: "제품군", dataIndex: "name", width: 150, ellipsis: true },
  { title: "분류", dataIndex: "category", width: 88 },
  { title: "규격", dataIndex: "spec", width: 140, ellipsis: true },
  { title: "단위", dataIndex: "unit", width: 72 },
  {
    title: "수율",
    dataIndex: "yieldRate",
    width: 72,
    render: (value: number) => `${value.toFixed(1)}%`,
  },
  {
    title: "리드타임",
    dataIndex: "leadTimeDay",
    width: 88,
    render: (value: number) => `${value}일`,
  },
  { title: "공정", dataIndex: "process", width: 96 },
  {
    title: "월생산능력",
    dataIndex: "monthlyCapacity",
    width: 110,
    render: (value: number) => value.toLocaleString("ko-KR"),
  },
  { title: "라인", dataIndex: "lineName", width: 100, ellipsis: true },
  { title: "공장", dataIndex: "plantName", width: 110, ellipsis: true },
  { title: "담당", dataIndex: "owner", width: 72 },
  { title: "리비전", dataIndex: "revision", width: 72 },
];

interface Props {
  open: boolean;
  mode: "single" | "multiple";
  title?: string;
  hint?: string;
  query: ProductGroupQuery;
  value: string[];
  onCancel: () => void;
  onConfirm: (codes: string[]) => void;
}

export function ProductGroupLookupModal({
  open,
  mode,
  title,
  hint,
  query,
  value,
  onCancel,
  onConfirm,
}: Props) {
  const [keyword, setKeyword] = useState("");
  const [draft, setDraft] = useState<string[]>(value);
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<ProductGroupMaster[]>([]);
  const [total, setTotal] = useState(0);
  const [elapsedMs, setElapsedMs] = useState(0);
  const [requestUrl, setRequestUrl] = useState("");

  const resultCodes = rows.map((row) => row.code);
  const draftAll = resultCodes.length > 0 && isAllSelected(
    draft.filter((code) => resultCodes.includes(code)),
    resultCodes,
  );

  const load = async (nextKeyword: string) => {
    setLoading(true);
    const result = await fetchProductGroups({ ...query, keyword: nextKeyword });
    setRows(result.rows);
    setTotal(result.total);
    setElapsedMs(result.elapsedMs);
    setRequestUrl(result.requestUrl);
    setLoading(false);
  };

  useEffect(() => {
    if (!open) return;
    setKeyword("");
    setDraft(value);
    void load("");
    // 팝업을 열 때만 현재 행/조건으로 조회한다.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  const confirmDraft = (codes: string[]) => {
    onConfirm(mode === "single" ? codes.slice(0, 1) : codes);
  };

  return (
    <Modal
      title={title ?? (mode === "single" ? "제품군 조회 (행 조건 API)" : "제품군 조회 (조건 API)")}
      open={open}
      onCancel={onCancel}
      width={1120}
      destroyOnClose
      footer={[
        mode === "multiple" ? (
          <Button key="all" disabled={!rows.length} onClick={() => setDraft(resultCodes)}>
            조회결과 전체 ({rows.length})
          </Button>
        ) : null,
        <Button key="clear" onClick={() => setDraft([])}>
          선택해제
        </Button>,
        <Button key="cancel" onClick={onCancel}>
          취소
        </Button>,
        <Button
          key="ok"
          type="primary"
          disabled={mode === "single" && draft.length === 0}
          onClick={() => confirmDraft(draft)}
        >
          {mode === "single" ? "선택" : `확인 (${draftAll ? "전체선택" : `${draft.length}개`})`}
        </Button>,
      ]}
    >
      <div className="popup-api-bar">
        <code>{requestUrl || "GET /api/product-groups"}</code>
        <span>{loading ? "조회 중..." : `응답 ${total.toLocaleString("ko-KR")}건 · ${elapsedMs}ms`}</span>
      </div>
      <div className="popup-selected-summary">
        <strong>
          {mode === "single"
            ? draft[0]
              ? `선택한 제품군 ${draft[0]}`
              : "조회 결과에서 한 건을 고르세요"
            : draft.length
              ? `선택한 값 ${draft.length}개`
              : "아직 고른 항목이 없습니다."}
        </strong>
        <p>{hint ?? "상위 조건으로 mock API를 호출합니다. 키워드를 넣으면 같은 조건으로 다시 조회합니다."}</p>
      </div>
      <div className="popup-api-search">
        <Input
          allowClear
          placeholder="제품군 / 코드 / 분류 / 규격 / 공정 / 담당자"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onPressEnter={() => void load(keyword)}
        />
        <Button type="primary" icon={<SearchOutlined />} loading={loading} onClick={() => void load(keyword)}>
          조회
        </Button>
      </div>
      <Spin spinning={loading}>
        {rows.length === 0 && !loading ? (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="조건에 맞는 제품군이 없습니다" />
        ) : (
          <Table
            size="small"
            rowKey="code"
            columns={COLUMNS}
            dataSource={rows}
            pagination={{ pageSize: 12, showSizeChanger: false, showTotal: (count) => `${count}건` }}
            scroll={{ x: 1400, y: 360 }}
            onRow={(row) => ({
              onDoubleClick: () => {
                if (mode === "single") confirmDraft([row.code]);
              },
            })}
            rowClassName={(row) => (draft.includes(row.code) ? "popup-row-picked" : "")}
            rowSelection={{
              type: mode === "single" ? "radio" : "checkbox",
              selectedRowKeys: draft,
              onChange: (keys) => setDraft(mode === "single" ? (keys as string[]).slice(-1) : (keys as string[])),
              preserveSelectedRowKeys: mode === "multiple",
            }}
          />
        )}
      </Spin>
    </Modal>
  );
}
