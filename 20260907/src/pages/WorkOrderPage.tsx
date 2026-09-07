import { InquiryScreen } from "./InquiryScreen";

export function WorkOrderPage() {
  return (
    <InquiryScreen
      productGroupEditor="select"
      hint="체크한 행만 저장합니다. 셀을 누르면 Select 에디터가 열리고, 고른 값이 바로 반영됩니다."
      excelName="작업지시_조회"
    />
  );
}
