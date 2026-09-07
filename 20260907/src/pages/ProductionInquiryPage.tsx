import { InquiryScreen } from "./InquiryScreen";

export function ProductionInquiryPage() {
  return (
    <InquiryScreen
      productGroupEditor="popup"
      hint="체크한 행만 저장합니다. 제품군은 셀을 눌러 API 팝업에서 조회한 뒤 고릅니다."
      excelName="생산실적_조회"
    />
  );
}
