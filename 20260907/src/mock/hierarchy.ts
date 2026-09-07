import type { CodeItem, FieldKey, InquiryRow, ProductGroupMaster } from "../types";
import { FIELD_META } from "../types";

interface Named {
  name: string;
  children?: Named[];
}

const TREE: Named[] = [
  {
    name: "서울법인",
    children: [
      {
        name: "반도체사업부",
        children: [
          {
            name: "기흥1공장",
            children: [
              {
                name: "A라인",
                children: [
                  { name: "DRAM", children: [{ name: "대기" }, { name: "생산중" }, { name: "검사중" }, { name: "완료" }] },
                  { name: "NAND", children: [{ name: "대기" }, { name: "생산중" }, { name: "완료" }] },
                ],
              },
              {
                name: "B라인",
                children: [
                  { name: "Foundry", children: [{ name: "대기" }, { name: "생산중" }, { name: "검사중" }, { name: "보류" }] },
                ],
              },
            ],
          },
          {
            name: "기흥2공장",
            children: [
              {
                name: "C라인",
                children: [
                  { name: "DRAM", children: [{ name: "대기" }, { name: "생산중" }, { name: "완료" }] },
                  { name: "CIS", children: [{ name: "대기" }, { name: "검사중" }, { name: "완료" }] },
                ],
              },
            ],
          },
        ],
      },
      {
        name: "디스플레이사업부",
        children: [
          {
            name: "탕정공장",
            children: [
              {
                name: "OLED1라인",
                children: [
                  { name: "OLED", children: [{ name: "대기" }, { name: "생산중" }, { name: "검사중" }, { name: "완료" }] },
                ],
              },
              {
                name: "OLED2라인",
                children: [
                  { name: "OLED", children: [{ name: "생산중" }, { name: "완료" }, { name: "보류" }] },
                  { name: "편광필름", children: [{ name: "대기" }, { name: "완료" }] },
                ],
              },
            ],
          },
        ],
      },
      {
        name: "전지사업부",
        children: [
          {
            name: "천안공장",
            children: [
              {
                name: "원통라인",
                children: [
                  { name: "원통형", children: [{ name: "대기" }, { name: "생산중" }, { name: "완료" }] },
                ],
              },
              {
                name: "파우치라인",
                children: [
                  { name: "파우치형", children: [{ name: "대기" }, { name: "생산중" }, { name: "검사중" }, { name: "보류" }] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    name: "경기법인",
    children: [
      {
        name: "반도체사업부",
        children: [
          {
            name: "화성공장",
            children: [
              {
                name: "P1라인",
                children: [
                  { name: "DRAM", children: [{ name: "대기" }, { name: "생산중" }, { name: "완료" }] },
                  { name: "NAND", children: [{ name: "대기" }, { name: "검사중" }, { name: "완료" }] },
                ],
              },
              {
                name: "P2라인",
                children: [
                  { name: "Foundry", children: [{ name: "대기" }, { name: "생산중" }, { name: "보류" }] },
                ],
              },
            ],
          },
        ],
      },
      {
        name: "부품사업부",
        children: [
          {
            name: "수원공장",
            children: [
              {
                name: "모듈라인",
                children: [
                  { name: "카메라모듈", children: [{ name: "대기" }, { name: "생산중" }, { name: "완료" }] },
                ],
              },
            ],
          },
          {
            name: "평택공장",
            children: [
              {
                name: "기판라인",
                children: [
                  { name: "PCB", children: [{ name: "대기" }, { name: "검사중" }, { name: "완료" }, { name: "보류" }] },
                ],
              },
            ],
          },
        ],
      },
      {
        name: "장비사업부",
        children: [
          {
            name: "안성공장",
            children: [
              {
                name: "조립라인",
                children: [
                  { name: "증착장비", children: [{ name: "대기" }, { name: "생산중" }, { name: "완료" }] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    name: "부산법인",
    children: [
      {
        name: "기계사업부",
        children: [
          {
            name: "사상공장",
            children: [
              {
                name: "가공1라인",
                children: [
                  { name: "정밀부품", children: [{ name: "대기" }, { name: "생산중" }, { name: "검사중" }, { name: "완료" }] },
                ],
              },
              {
                name: "가공2라인",
                children: [
                  { name: "금형", children: [{ name: "대기" }, { name: "완료" }, { name: "보류" }] },
                ],
              },
            ],
          },
        ],
      },
      {
        name: "소재사업부",
        children: [
          {
            name: "울산공장",
            children: [
              {
                name: "화합물라인",
                children: [
                  { name: "고순도소재", children: [{ name: "대기" }, { name: "생산중" }, { name: "완료" }] },
                ],
              },
            ],
          },
          {
            name: "창원공장",
            children: [
              {
                name: "압연라인",
                children: [
                  { name: "특수강", children: [{ name: "대기" }, { name: "검사중" }, { name: "완료" }] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    name: "대구법인",
    children: [
      {
        name: "디스플레이사업부",
        children: [
          {
            name: "구미공장",
            children: [
              {
                name: "LCD라인",
                children: [
                  { name: "LCD", children: [{ name: "대기" }, { name: "생산중" }, { name: "완료" }] },
                ],
              },
              {
                name: "OLED라인",
                children: [
                  { name: "OLED", children: [{ name: "대기" }, { name: "생산중" }, { name: "검사중" }, { name: "보류" }] },
                ],
              },
            ],
          },
        ],
      },
      {
        name: "전지사업부",
        children: [
          {
            name: "구미전지공장",
            children: [
              {
                name: "ESS라인",
                children: [
                  { name: "ESS", children: [{ name: "대기" }, { name: "생산중" }, { name: "완료" }] },
                ],
              },
            ],
          },
        ],
      },
      {
        name: "물류사업부",
        children: [
          {
            name: "왜관물류센터",
            children: [
              {
                name: "출하라인",
                children: [
                  { name: "완제품출하", children: [{ name: "대기" }, { name: "완료" }] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
];

export type HierarchyPath = Pick<InquiryRow, "corp" | "division" | "plant" | "line" | "productGroup" | "status">;

export const catalogs: Record<FieldKey, CodeItem[]> = {
  corp: [],
  division: [],
  plant: [],
  line: [],
  productGroup: [],
  status: [],
  customer: [],
  worker: [],
  equipment: [],
  warehouse: [],
  item: [],
  shipTo: [],
  inspectType: [],
};

const childrenByParent: Record<FieldKey, Map<string, CodeItem[]>> = {
  corp: new Map(),
  division: new Map(),
  plant: new Map(),
  line: new Map(),
  productGroup: new Map(),
  status: new Map(),
  customer: new Map(),
  worker: new Map(),
  equipment: new Map(),
  warehouse: new Map(),
  item: new Map(),
  shipTo: new Map(),
  inspectType: new Map(),
};

const CUSTOMERS_BY_CORP_NAME: Record<string, string[]> = {
  서울법인: ["삼성전자", "SK하이닉스", "LG디스플레이", "삼성SDI", "LG전자", "한화오션", "두산에너빌리티", "롯데케미칼", "코오롱인더", "효성중공업", "DB하이텍", "원익IPS"],
  경기법인: ["삼성전자", "현대모비스", "LS일렉트릭", "LG이노텍", "만도", "한온시스템", "현대위아", "SL", "서연이화", "화신", "명화공업", "한국타이어"],
  부산법인: ["현대모비스", "두산로보틱스", "포스코인터내셔널", "현대중공업", "한진중공업", "STX엔진", "대우조선해양", "세진중공업", "삼강엠앤티", "동성화인텍", "태광산업", "한국카본"],
  대구법인: ["한화시스템", "LG디스플레이", "LS일렉트릭", "구미전자", "LG화학", "SK온", "에스엘", "경창산업", "평화홀딩스", "서진오토모티브", "덕양산업", "화승알앤에이"],
};

const WORKER_NAMES = [
  "김한결",
  "박서준",
  "최은우",
  "이도윤",
  "정하린",
  "윤서아",
  "한지호",
  "강도윤",
  "오세린",
  "신유진",
  "배준혁",
  "문채원",
  "서지안",
  "임태윤",
  "곽민재",
  "노하늘",
  "하은성",
  "조예린",
];

const INSPECT_TYPES = [
  "외관검사",
  "치수검사",
  "전기검사",
  "신뢰성검사",
  "누설검사",
  "토크검사",
  "비전검사",
  "X-ray검사",
  "기능검사",
  "최종검사",
  "출하검사",
  "샘플링검사",
];

const PRODUCT_GROUP_TEMPLATES = [
  { name: "DRAM DDR4 8Gb", category: "메모리", spec: "1.2V / 3200Mbps", unit: "wafer" },
  { name: "DRAM DDR5 16Gb", category: "메모리", spec: "1.1V / 4800Mbps", unit: "wafer" },
  { name: "DRAM DDR5 24Gb", category: "메모리", spec: "1.1V / 5600Mbps", unit: "wafer" },
  { name: "NAND 256Gb TLC", category: "스토리지", spec: "3D 128단", unit: "wafer" },
  { name: "NAND 512Gb QLC", category: "스토리지", spec: "3D 176단", unit: "wafer" },
  { name: "NAND 1Tb QLC", category: "스토리지", spec: "3D 236단", unit: "wafer" },
  { name: "Foundry 7nm", category: "파운드리", spec: "HPC / Mobile", unit: "wafer" },
  { name: "Foundry 5nm", category: "파운드리", spec: "EUV", unit: "wafer" },
  { name: "Foundry 3nm", category: "파운드리", spec: "GAA", unit: "wafer" },
  { name: "CIS 48MP", category: "센서", spec: "0.8um pixel", unit: "ea" },
  { name: "CIS 108MP", category: "센서", spec: "0.7um pixel", unit: "ea" },
  { name: "OLED 6.7인치", category: "디스플레이", spec: "120Hz / LTPO", unit: "panel" },
  { name: "OLED 8.6인치", category: "디스플레이", spec: "Tablet AMOLED", unit: "panel" },
  { name: "OLED 15.6인치", category: "디스플레이", spec: "Notebook RGB", unit: "panel" },
  { name: "LCD 27인치", category: "디스플레이", spec: "QHD IPS", unit: "panel" },
  { name: "LCD 32인치", category: "디스플레이", spec: "4K VA", unit: "panel" },
  { name: "편광필름 POL", category: "소재", spec: "OLED용 TAC", unit: "m" },
  { name: "원통형 2170", category: "전지", spec: "4.8Ah", unit: "cell" },
  { name: "원통형 4680", category: "전지", spec: "25Ah", unit: "cell" },
  { name: "파우치형 72Ah", category: "전지", spec: "NCM811", unit: "cell" },
  { name: "파우치형 90Ah", category: "전지", spec: "NCA", unit: "cell" },
  { name: "ESS 2.5MWh", category: "전지", spec: "LFP Rack", unit: "rack" },
  { name: "카메라모듈 48M", category: "부품", spec: "OIS / AF", unit: "ea" },
  { name: "카메라모듈 200M", category: "부품", spec: "HPB", unit: "ea" },
  { name: "PCB 8Layer", category: "부품", spec: "HDI 0.4T", unit: "ea" },
  { name: "PCB 12Layer", category: "부품", spec: "Any-layer", unit: "ea" },
  { name: "증착장비 CVD", category: "장비", spec: "300mm", unit: "set" },
  { name: "증착장비 PVD", category: "장비", spec: "High-k", unit: "set" },
  { name: "정밀부품 베어링", category: "기계", spec: "P4급", unit: "ea" },
  { name: "금형 프레스", category: "기계", spec: "600ton", unit: "set" },
  { name: "고순도소재 HF", category: "소재", spec: "EL 등급", unit: "kg" },
  { name: "특수강 SUS316", category: "소재", spec: "냉연 2B", unit: "kg" },
  { name: "완제품출하 BOX", category: "물류", spec: "표준팔레트", unit: "box" },
  { name: "HBM3E 24Gb", category: "메모리", spec: "8Hi / 1024GB/s", unit: "wafer" },
  { name: "LPDDR5X 16Gb", category: "메모리", spec: "8533Mbps", unit: "wafer" },
  { name: "UFS 4.0 512GB", category: "스토리지", spec: "모듈 / 컨트롤러", unit: "ea" },
];

export const validPaths: HierarchyPath[] = [];
export const productGroupMasters: ProductGroupMaster[] = [];
export const nameByCode = new Map<string, string>();

const namedKeys = new Set<string>();

function addItem(
  field: "corp" | "division" | "plant" | "line" | "productGroup" | "status",
  name: string,
  parentCode: string | null,
  seq: number,
): string {
  const prefix = { corp: "C", division: "D", plant: "P", line: "L", productGroup: "G", status: "S" }[field];
  const width = field === "status" ? 5 : field === "productGroup" ? 4 : 3;
  const code = `${prefix}${String(seq).padStart(width, "0")}`;
  catalogs[field].push({ code, name, parentCode });
  nameByCode.set(code, name);
  indexChild(field, { code, name, parentCode });
  return code;
}

function addNamed(field: FieldKey, name: string, parentCode: string, code = name) {
  const key = `${field}|${code}|${parentCode}`;
  if (namedKeys.has(key)) {
    nameByCode.set(code, name);
    return;
  }
  namedKeys.add(key);
  catalogs[field].push({ code, name, parentCode });
  indexChild(field, { code, name, parentCode });
  nameByCode.set(code, name);
}

function indexChild(field: FieldKey, item: CodeItem) {
  const parent = item.parentCode ?? "__root__";
  const list = childrenByParent[field].get(parent) ?? [];
  list.push(item);
  childrenByParent[field].set(parent, list);
}

function hashCode(value: string) {
  let hash = 0;
  for (let i = 0; i < value.length; i += 1) {
    hash = (hash * 31 + value.charCodeAt(i)) | 0;
  }
  return Math.abs(hash);
}

function chainFromLine(lineCode: string) {
  const line = catalogs.line.find((item) => item.code === lineCode);
  if (!line?.parentCode) return null;
  const plant = catalogs.plant.find((item) => item.code === line.parentCode);
  if (!plant?.parentCode) return null;
  const division = catalogs.division.find((item) => item.code === plant.parentCode);
  if (!division?.parentCode) return null;
  const corp = catalogs.corp.find((item) => item.code === division.parentCode);
  if (!corp) return null;
  return { line, plant, division, corp };
}

function templateForName(name: string, salt: string) {
  const exact = PRODUCT_GROUP_TEMPLATES.find((item) => item.name === name);
  if (exact) return exact;
  const byPrefix = PRODUCT_GROUP_TEMPLATES.find((item) => item.name.startsWith(name) || name.startsWith(item.name.split(" ")[0]));
  if (byPrefix) return byPrefix;
  return PRODUCT_GROUP_TEMPLATES[hashCode(salt) % PRODUCT_GROUP_TEMPLATES.length];
}

const PROCESS_BY_CATEGORY: Record<string, string> = {
  메모리: "Front-end",
  스토리지: "Front-end",
  파운드리: "Front-end",
  센서: "Front-end",
  디스플레이: "Panel",
  소재: "Chemical",
  전지: "Cell",
  부품: "Module",
  장비: "Assembly",
  기계: "Machining",
  물류: "Outbound",
};

const OWNERS = ["김한결", "박서준", "최은우", "이도윤", "정하린", "윤서아", "한지호", "강도윤"];
const GROUP_STATUSES = ["대기", "생산중", "검사중", "완료"];
const ITEM_GRADES = ["표준", "고신뢰", "저전력", "고용량", "수출용", "내수용", "A급", "엔지니어샘플"];

function toProductGroupMaster(group: CodeItem): ProductGroupMaster | null {
  if (!group.parentCode) return null;
  const chain = chainFromLine(group.parentCode);
  if (!chain) return null;
  const tpl = templateForName(group.name, group.code);
  const hash = hashCode(group.code);
  return {
    code: group.code,
    name: group.name,
    parentCode: group.parentCode,
    category: group.name === "공통제품군" ? "공통" : tpl.category,
    spec: group.name === "공통제품군" ? "라인 공통 / 표준규격" : tpl.spec,
    unit: tpl.unit,
    yieldRate: Number((88 + (hash % 11) + ((hash >> 5) % 10) / 10).toFixed(1)),
    leadTimeDay: 3 + (hash % 21),
    process: PROCESS_BY_CATEGORY[tpl.category] ?? "Assembly",
    owner: OWNERS[hash % OWNERS.length],
    revision: `R${1 + (hash % 9)}`,
    monthlyCapacity: 8000 + (hash % 40) * 250,
    lineCode: chain.line.code,
    lineName: chain.line.name,
    plantCode: chain.plant.code,
    plantName: chain.plant.name,
    divisionCode: chain.division.code,
    divisionName: chain.division.name,
    corpCode: chain.corp.code,
    corpName: chain.corp.name,
  };
}

(function build() {
  const counters = {
    corp: 0,
    division: 0,
    plant: 0,
    line: 0,
    productGroup: 0,
    status: 0,
  };

  TREE.forEach((corp) => {
    const corpCode = addItem("corp", corp.name, null, ++counters.corp);
    for (const customerName of CUSTOMERS_BY_CORP_NAME[corp.name] ?? []) {
      addNamed("customer", customerName, corpCode);
    }
    corp.children?.forEach((division) => {
      const divisionCode = addItem("division", division.name, corpCode, ++counters.division);
      division.children?.forEach((plant) => {
        const plantCode = addItem("plant", plant.name, divisionCode, ++counters.plant);
        plant.children?.forEach((line) => {
          const lineCode = addItem("line", line.name, plantCode, ++counters.line);
          line.children?.forEach((group) => {
            const groupCode = addItem("productGroup", group.name, lineCode, ++counters.productGroup);
            group.children?.forEach((status) => {
              const statusCode = addItem("status", status.name, groupCode, ++counters.status);
              validPaths.push({
                corp: corpCode,
                division: divisionCode,
                plant: plantCode,
                line: lineCode,
                productGroup: groupCode,
                status: statusCode,
              });
            });
          });
        });
      });
    });
  });

  Object.entries(CUSTOMERS_BY_CORP_NAME).forEach(([, names]) => {
    names.forEach((customerName) => {
      WORKER_NAMES.forEach((workerName) => addNamed("worker", workerName, customerName));
      Array.from({ length: 12 }, (_, index) => {
        const seq = String(index + 1).padStart(2, "0");
        addNamed("shipTo", `출하처 ${seq}`, customerName, `SHIP-${customerName}-${seq}`);
      });
    });
  });

  catalogs.corp.forEach((corp) => {
    INSPECT_TYPES.forEach((typeName, index) => {
      addNamed("inspectType", typeName, corp.code, `INSP-${corp.code}-${String(index + 1).padStart(2, "0")}`);
    });
    Array.from({ length: 12 }, (_, index) => {
      const seq = String(index + 1).padStart(2, "0");
      addNamed("warehouse", `${corp.name.replace("법인", "")} ${seq}창고`, corp.code, `WH-${corp.code}-${seq}`);
    });
  });

  const plantsByDivision = new Map<string, number>();
  catalogs.plant.forEach((plant) => {
    if (!plant.parentCode) return;
    plantsByDivision.set(plant.parentCode, (plantsByDivision.get(plant.parentCode) ?? 0) + 1);
  });

  catalogs.division.forEach((division) => {
    let count = plantsByDivision.get(division.code) ?? 0;
    while (count < 12) {
      count += 1;
      const plantCode = addItem(
        "plant",
        `${division.name.replace("사업부", "")} ${String(count).padStart(2, "0")}공장`,
        division.code,
        ++counters.plant,
      );
      const lineCode = addItem("line", `${count}라인`, plantCode, ++counters.line);
      const groupCode = addItem("productGroup", "공통제품군", lineCode, ++counters.productGroup);
      ["대기", "생산중", "완료"].forEach((statusName) => {
        const statusCode = addItem("status", statusName, groupCode, ++counters.status);
        validPaths.push({
          corp: division.parentCode ?? "",
          division: division.code,
          plant: plantCode,
          line: lineCode,
          productGroup: groupCode,
          status: statusCode,
        });
      });
    }
  });

  catalogs.line.forEach((line) => {
    const existingNames = new Set(
      catalogs.productGroup.filter((group) => group.parentCode === line.code).map((group) => group.name),
    );
    PRODUCT_GROUP_TEMPLATES.forEach((tpl) => {
      if (existingNames.has(tpl.name)) return;
      const groupCode = addItem("productGroup", tpl.name, line.code, ++counters.productGroup);
      GROUP_STATUSES.forEach((statusName) => {
        const statusCode = addItem("status", statusName, groupCode, ++counters.status);
        const chain = chainFromLine(line.code);
        if (!chain) return;
        validPaths.push({
          corp: chain.corp.code,
          division: chain.division.code,
          plant: chain.plant.code,
          line: line.code,
          productGroup: groupCode,
          status: statusCode,
        });
      });
    });
  });

  catalogs.plant.forEach((plant) => {
    Array.from({ length: 15 }, (_, index) => {
      const seq = String(index + 1).padStart(2, "0");
      addNamed("equipment", `설비 ${seq}`, plant.code, `EQ-${plant.code}-${seq}`);
    });
  });

  catalogs.productGroup.forEach((group) => {
    ITEM_GRADES.forEach((grade, index) => {
      const seq = String(index + 1).padStart(2, "0");
      addNamed("item", `${group.name} ${grade}`, group.code, `IT-${group.code}-${seq}`);
    });
  });

  catalogs.productGroup.forEach((group) => {
    const master = toProductGroupMaster(group);
    if (master) productGroupMasters.push(master);
  });
})();

export function codeName(code: string | undefined | null): string {
  if (!code) return "";
  return nameByCode.get(code) ?? code;
}

export function formatCode(code: string | undefined | null): string {
  if (!code) return "";
  const name = nameByCode.get(code);
  return name ? `${name}` : code;
}

function uniqueByCode(items: CodeItem[]): CodeItem[] {
  const seen = new Set<string>();
  return items.filter((item) => {
    if (seen.has(item.code)) return false;
    seen.add(item.code);
    return true;
  });
}

function selectedCodes(value: string | string[] | undefined): string[] {
  if (Array.isArray(value)) return value;
  return value ? [value] : [];
}

export function getOptions(
  field: FieldKey,
  context: Partial<Record<FieldKey, string | string[] | undefined>>,
): CodeItem[] {
  if (field === "corp") return catalogs.corp;
  const parentField = parentFieldOf(field);
  if (!parentField) return catalogs[field];
  const parents = selectedCodes(context[parentField]);
  if (parents.length === 0) return [];
  return uniqueByCode(parents.flatMap((parent) => childrenByParent[field].get(parent) ?? []));
}

export function parentFieldOf(field: FieldKey): FieldKey | null {
  return FIELD_META.find((item) => item.key === field)?.parent ?? null;
}

export function childFieldsOf(field: FieldKey): FieldKey[] {
  const result: FieldKey[] = [];
  const walk = (key: FieldKey) => {
    FIELD_META.filter((item) => item.parent === key).forEach((child) => {
      result.push(child.key);
      walk(child.key);
    });
  };
  walk(field);
  return result;
}

export function cascadeRow(row: InquiryRow, changedField: FieldKey): InquiryRow {
  const next: InquiryRow = { ...row };
  for (const field of childFieldsOf(changedField)) {
    const options = getOptions(field, next);
    if (!options.some((item) => item.code === next[field])) {
      next[field] = "";
    }
  }
  return next;
}

export function keepValidCodes(field: FieldKey, selected: string[], context: Partial<Record<FieldKey, string | string[] | undefined>>): string[] {
  const allowed = new Set(getOptions(field, context).map((item) => item.code));
  return selected.filter((code) => allowed.has(code));
}
