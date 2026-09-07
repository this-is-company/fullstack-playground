import { Menu } from "antd";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { APP_MENU, findMenuByPath } from "../app/menu";

export function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const current = findMenuByPath(location.pathname);

  return (
    <div className="app-shell">
      <aside className="app-sider">
        <div className="app-sider-brand">
          <strong>MES</strong>
          <span>조회 시스템</span>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={APP_MENU.map((item) => item.key)}
          items={APP_MENU.map((group) => ({
            key: group.key,
            label: group.label,
            children: group.children?.map((item) => ({
              key: item.path ?? item.key,
              label: item.label,
            })),
          }))}
          onClick={({ key }) => {
            if (key.startsWith("/")) navigate(key);
          }}
        />
      </aside>
      <div className="app-content">
        <header className="app-header">
          <div>
            <p className="eyebrow">생산관리</p>
            <h1>{current?.label ?? "조회"}</h1>
          </div>
        </header>
        <div className="app-body">
          <main className="app-main">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}
