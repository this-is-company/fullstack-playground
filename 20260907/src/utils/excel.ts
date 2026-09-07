import type { InquiryRow } from "../types";
import { FIELD_META } from "../types";
import { formatCode } from "../mock/hierarchy";

function escapeXml(value: string) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function cell(value: string | number, type: "String" | "Number" = "String") {
  return `<Cell><Data ss:Type="${type}">${escapeXml(String(value))}</Data></Cell>`;
}

export function downloadExcel(rows: InquiryRow[], fileName: string) {
  const headers = ["주문번호", ...FIELD_META.map((item) => item.label), "수량", "금액", "주문일"];
  const body = rows.map((row) => {
    const values: Array<{ value: string | number; type: "String" | "Number" }> = [
      { value: row.orderNo, type: "String" },
      ...FIELD_META.map((meta) => ({ value: formatCode(row[meta.key]) || "", type: "String" as const })),
      { value: row.qty, type: "Number" },
      { value: row.amount, type: "Number" },
      { value: row.orderDate, type: "String" },
    ];
    return `<Row>${values.map((item) => cell(item.value, item.type)).join("")}</Row>`;
  });

  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
  <Worksheet ss:Name="생산실적">
    <Table>
      <Row>${headers.map((header) => cell(header)).join("")}</Row>
      ${body.join("\n")}
    </Table>
  </Worksheet>
</Workbook>`;

  const blob = new Blob([`\uFEFF${xml}`], { type: "application/vnd.ms-excel;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName.endsWith(".xls") ? fileName : `${fileName}.xls`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
