export interface AppMenuItem {
  key: string;
  label: string;
  path?: string;
  children?: AppMenuItem[];
}

export const APP_MENU: AppMenuItem[] = [
  {
    key: "production",
    label: "생산관리",
    children: [
      { key: "/production", path: "/production", label: "생산실적 조회" },
      { key: "/work-order", path: "/work-order", label: "작업지시 조회" },
    ],
  },
];

export function flattenMenu(items: AppMenuItem[] = APP_MENU): AppMenuItem[] {
  return items.flatMap((item) => (item.children ? flattenMenu(item.children) : [item]));
}

export function findMenuByPath(pathname: string) {
  return flattenMenu().find((item) => item.path === pathname);
}
