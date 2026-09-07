import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "./layout/AppLayout";
import { ProductionInquiryPage } from "./pages/ProductionInquiryPage";
import { WorkOrderPage } from "./pages/WorkOrderPage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/production" replace />} />
          <Route path="/production" element={<ProductionInquiryPage />} />
          <Route path="/work-order" element={<WorkOrderPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
