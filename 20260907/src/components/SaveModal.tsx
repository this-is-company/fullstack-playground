import { Modal, Table } from "antd";
import type { RowChange } from "../types";

interface Props {
  open: boolean;
  loading?: boolean;
  changes: RowChange[];
  skipped: number;
  onCancel: () => void;
  onConfirm: () => void;
}

export function SaveModal({ open, loading, changes, skipped, onCancel, onConfirm }: Props) {
  const rows = changes.flatMap((item) =>
    item.changes.map((change, index) => ({
      key: `${item.id}-${change.field}`,
      orderNo: item.orderNo,
      orderNoRowSpan: index === 0 ? item.changes.length : 0,
      label: change.label,
      from: change.from,
      to: change.to,
    })),
  );

  return (
    <Modal
      title="저장할 변경 내용"
      open={open}
      onCancel={onCancel}
      onOk={onConfirm}
      confirmLoading={loading}
      okText="저장"
      cancelText="취소"
      width={760}
    >
      <p className="save-modal-summary">
        체크한 행 중 수정된 <b>{changes.length}</b>건을 저장합니다
        {skipped ? ` · 변경 없는 체크 ${skipped}건은 제외` : ""}.
      </p>
      <Table
        size="small"
        pagination={false}
        scroll={{ y: 360 }}
        dataSource={rows}
        columns={[
          {
            title: "주문번호",
            dataIndex: "orderNo",
            width: 150,
            onCell: (row) => ({ rowSpan: row.orderNoRowSpan }),
          },
          { title: "항목", dataIndex: "label", width: 100 },
          { title: "변경 전", dataIndex: "from" },
          { title: "변경 후", dataIndex: "to" },
        ]}
      />
    </Modal>
  );
}
