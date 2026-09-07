# Select 변경 반영과 연쇄조건 구현

조회조건 Select와 AG Grid 셀 Select를 바꿨을 때, 컴포넌트가 해야 하는 일과 연쇄(cascade) 구현을 정리한다.  
실적 데이터 조회(조회 버튼, 페이징)와 코드 마스터(법인·공장 옵션)는 별개다.

관련 코드:

- 공통 트리: `src/types.ts` (`FIELD_META`)
- 옵션/연쇄: `src/mock/hierarchy.ts` (`getOptions`, `cascadeRow`, `keepValidCodes`)
- 조회조건: `src/components/SearchForm.tsx` (`cascadeSearch`)
- 그리드 에디터: `src/components/SelectCellEditor.tsx`
- 그리드 반영: `src/components/InquiryGrid.tsx` (`valueSetter`, `onCellValueChanged`)

---

## 1. 역할을 두 층으로 나눈다

| 층 | 담당 | 하면 안 되는 일 |
|---|---|---|
| Select 자신 | 새 값(코드)을 확정해서 부모/그리드에 넘긴다 | 하위 필드를 직접 지우고, 옵션 API를 제각각 호출 |
| 부모 (조회폼 / 그리드) | 그 값으로 하위 옵션을 거르고, 성립하지 않는 선택을 제거한다 | 에디터 안에서 React `rowData`를 바꿔 편집을 중단시킴 |

연쇄 로직을 Select 안에 넣으면 조회조건과 그리드가 서로 다른 구현이 되고, “셀에서 바꿨는데 그리드에 안 남음” 같은 버그가 난다.  
옵션 필터링은 `getOptions` / `cascadeSearch` / `cascadeRow`가 하고, Select는 값만 확정한다.

---

## 2. Select를 수정했을 때 컴포넌트가 해야 하는 일

### 2.1 조회조건 (Ant Design Select)

`onChange`에서 **선택한 코드만** 올린다. 하위 옵션 계산은 `apply` → `cascadeSearch`가 한다.

```ts
function fieldControl(meta, value, apply) {
  const optionItems = getOptions(meta.key, value);

  <Select
    value={value[meta.key]}
    options={optionItems.map((item) => ({ value: item.code, label: item.name }))}
    onChange={(code) => apply(meta.key, code)}
  />
}
```

`apply`는 필드 값만 덮어쓴 뒤 연쇄에 넘긴다.

```ts
const apply = (field, nextValue) => {
  onChange(cascadeSearch({ ...value, [field]: nextValue }, field), field);
};
```

조회조건 Select가 할 일:

1. **현재 상위 값으로 옵션을 받는다.** `getOptions(field, value)` 결과만 그린다. 상위를 안 고르면 옵션 0개 → disabled.
2. **고른 값(코드)만 부모에 알린다.** 화면 이름 문자열이 아니라 `code`.
3. **자기 아래 필드를 직접 지우지 않는다.** 부모의 `cascadeSearch` 책임이다.
4. 멀티 선택에서 “지금 목록을 전부 골랐는지”는 표시용이다 (`전체선택`). 저장/조회 파라미터는 여전히 code 배열이다.

### 2.2 그리드 셀 Select (AG Grid 32)

Ant Design과 달리, AG Grid 32 React 셀 에디터는 `useImperativeHandle`의 `getValue`만으로는 부족하다.  
그리드가 확정하는 값은 **`onValueChange`로 갱신된 값**이다.

```ts
const choose = (code: string) => {
  props.onValueChange(code); // 1) 그리드가 들고 있는 편집 값을 갱신
  props.stopEditing();       // 2) 편집 종료 → getValue()가 위 값을 반환
};
```

이 순서가 반대이거나 `onValueChange`를 빼면, 그리드는 편집 시작 값을 그대로 가져가서 **수정이 취소된 것처럼** 보인다.  
법인 셀을 바꿨는데 칸이 되돌아가던 원인이 여기다.

그리드 Select 에디터가 할 일:

1. **옵션은 그 행 데이터로 만든다.** `getOptions(field, props.data)`  
   예: 이 행의 법인이 서울이면 사업부는 서울 산하만.
2. **고르자마자 `onValueChange(새코드)`를 호출한다.**
3. **그다음에 `stopEditing()`으로 편집을 끝낸다.**
4. **에디터 안에서 `node.setDataValue` / React `setRows`를 하지 않는다.**  
   편집 도중에 `rowData`가 바뀌면 AG Grid가 에디터를 죽이면서 값을 되돌린다.
5. **팝업 안 클릭이 ‘셀 밖 클릭’으로 보이지 않게** mousedown 전파를 막는다.  
   Ant Design Select를 그리드에 그대로 넣고 드롭다운을 `document.body`로 빼면, 그리드가 포커스를 잃었다고 보고 에디터를 먼저 닫는다.

편집이 끝난 뒤 컬럼/행이 할 일:

```ts
valueSetter: (params) => {
  if (!params.data || params.oldValue === params.newValue) return false;
  params.data[field] = params.newValue;
  return true; // false면 그리드가 반영하지 않음
}

onCellValueChanged: (event) => {
  const next = cascadeRow({ ...event.data, [field]: newValue }, field);
  event.node.setData(next);
  onRowCommit(next, field, oldValue, newValue);
  event.api.refreshCells({ rowNodes: [event.node], columns: FIELD_KEYS, force: true });
}
```

- `valueSetter`: 행 객체에 새 코드를 넣고 `true`를 반환해야 그리드가 반영했다고 본다.
- `onCellValueChanged`: 하위 컬럼 정리, React state/원본 동기화, 해당 행만 다시 그린다.
- `valueFormatter`: 코드(`C001`)를 이름(`서울법인`)으로만 보여 준다. 저장 값은 코드로 둔다.

한 줄로 정리하면, **Select는 값을 확정만 하고, 연쇄는 에디터가 끝난 뒤 행 단위로 한다.**

---

## 3. 연쇄는 같은 트리, 적용 단위만 다르다

부모 관계는 `FIELD_META.parent` 하나다. 조회조건과 그리드가 트리를 따로 갖지 않는다.

```ts
{ key: "corp",     label: "법인",   mode: "single",      parent: null }
{ key: "division", label: "사업부", mode: "single",      parent: "corp" }
{ key: "plant",    label: "공장",   mode: "multiPopup",  parent: "division" }
{ key: "customer", label: "고객사", mode: "multiSelect", parent: "corp" }
{ key: "worker",   label: "작업자", mode: "multiSelect", parent: "customer" }
```

공통 함수:

| 함수 | 역할 |
|---|---|
| `getOptions(field, context)` | `context`의 부모 값으로 `parentCode`가 맞는 옵션만 반환 |
| `parentFieldOf(field)` | `FIELD_META.parent` |
| `childFieldsOf(field)` | 그 필드를 부모로 하는 모든 자손 (재귀) |
| `keepValidCodes` | 멀티 선택 중 새 옵션에 없는 코드 제거 |
| `cascadeRow` | 행의 자손 값이 새 옵션에 없으면 `""` |

```ts
export function getOptions(field, context) {
  if (field === "corp") return catalogs.corp;
  const parentField = parentFieldOf(field);
  const parents = selectedCodes(context[parentField]); // 싱글이면 [code], 멀티면 string[]
  if (parents.length === 0) return [];
  return catalogs[field].filter((item) => parents.includes(item.parentCode));
}
```

법인을 바꾸면 `childFieldsOf("corp")`에 사업부, 공장, 고객사, 창고 등이 포함된다.  
공장만 바꾸면 라인·설비만 다시 계산한다.

### 3.1 조회조건 연쇄 — `cascadeSearch`

상태 모양은 **조건 객체 하나**다. 공장은 `string[]`(멀티), 법인은 `string`(싱글).

```ts
function cascadeSearch(next, changedField) {
  const result = { ...next };
  for (const field of childFieldsOf(changedField)) {
    if (싱글) {
      // 지금 값이 새 옵션에 없으면 undefined
    } else {
      result[field] = keepValidCodes(field, result[field], result);
    }
  }
  return result;
}
```

흐름:

1. 법인 `onChange` → `{ ...value, corp: "C002" }`
2. 자손을 순서대로 훑는다. 사업부가 경기법인 산하가 아니면 지워진다.
3. 사업부가 지워지면 공장 옵션도 빈 배열 → 공장 선택도 제거된다.
4. React state가 새 `SearchValues`로 바뀌면, 각 Select는 `getOptions(자신의 키, 새 value)`로 다시 렌더된다.

조회조건은 “한 화면의 필터 상태”라서, 상위가 바뀌면 **아직 조회하지 않은 선택값까지** 미리 비운다.  
안 지우면 서울 법인 + 부산 공장처럼 성립하지 않는 조건으로 실적 API를 치게 된다.

### 3.2 그리드 연쇄 — `cascadeRow`

상태 모양은 **행 하나**다. 각 칸 값은 하나(`string`)다. 조회조건처럼 한 행에 공장을 여러 개 담지 않는다.

```ts
export function cascadeRow(row, changedField) {
  const next = { ...row };
  for (const field of childFieldsOf(changedField)) {
    const options = getOptions(field, next);
    if (!options.some((item) => item.code === next[field])) {
      next[field] = "";
    }
  }
  return next;
}
```

흐름:

1. 셀에서 법인 선택 → `onValueChange` + `stopEditing`
2. `valueSetter`가 `row.corp`만 변경
3. `onCellValueChanged`에서 `cascadeRow`로 사업부/공장 등이 새 법인에 없으면 `""`
4. `node.setData(next)`로 **그 행만** 교체
5. `refreshCells`로 이름 표시를 다시 그림 (`C002` → `경기법인`, 빈 하위는 `-`)

에디터 옵션이 행마다 다른 이유:

```ts
const options = useMemo(() => getOptions(field, props.data), [field, props.data]);
```

컬럼에 `cellEditorParams.values`를 고정하면, 1행 서울 / 2행 부산인데도 사업부 목록이 항상 같다.  
**반드시 행 데이터(`props.data`)로 옵션을 계산**한다.

비교:

| | 조회조건 | 그리드 |
|---|---|---|
| 상태 | `SearchValues` 하나 | `InquiryRow` 여러 건 중 한 행 |
| 공장 값 | `string[]` (멀티 필터) | `string` (그 행의 공장) |
| 연쇄 함수 | `cascadeSearch` | `cascadeRow` |
| 적용 범위 | 폼 전체 | 수정한 행만 |
| 트리 | `FIELD_META` 동일 | 동일 |

---

## 4. 마스터 옵션을 한 번에 받을지, 연쇄마다 API를 칠지

이 데모는 마스터를 메모리에 올려 두고 `getOptions`가 `parentCode`로 거른다.  
조회 버튼 / 페이지 / 엑셀만 “실적 API”다.

회사 화면의 데이터는 보통 두 종류다.

| 종류 | 예 | 언제 API |
|---|---|---|
| 마스터 (코드) | 법인, 사업부, 공장, 상태 | 화면 진입 시 또는 상위 변경 시 |
| 트랜잭션 (실적) | 생산실적 N건 | 조회 / 페이징 / 엑셀 / 저장 때만 |

조회조건·그리드 Select **옵션**은 마스터다. 실적 그리드 조회와 섞지 않는다.

### 4.1 한 번에 다 받기

적합한 경우: 코드가 수천 이하(법인 수십, 공장 수백, 라인 수천)이고, 권한별로 목록 차이가 크지 않을 때.

- 장점: 상위 바꿀 때마다 옵션 API가 없어서 Select가 바로 열린다. 그리드 셀 편집도 행마다 API를 안 친다.
- 단점: 첫 로딩이 커진다. 권한/사업부별로 목록이 전혀 다르면 과하게 받는다.

공장 수백 규모 MES/ERP에서 흔하다.  
진입 시 `GET /codes` 한 번 → `{ parentCode, code, name }[]` → 프론트 `getOptions`와 같은 필터.

### 4.2 상위 바꿀 때마다 API

적합한 경우: 공장·품목이 수만~수십만이라 상위 없이 전체를 받으면 응답이 클 때.

- 장점: 필요한 자식만 받는다. 권한 필터를 서버가 적용하기 쉽다.
- 단점: 조회조건은 참을 만하지만, **그리드에서 법인 셀을 열 때마다** `GET /divisions?corp=`를 치면 편집이 느리다.

### 4.3 실무 절충

1. **얕은 단계(법인, 사업부, 상태, 창고)는 화면 진입 시 한 번에** 받는다.
2. **깊은 단계(공장, 라인, 품목, 설비)만 상위 선택 시 API.**  
   - 조회조건: 사업부 `onChange` → `GET /plants?division=`  
   - 그리드: 같은 API를 행이 아니라 **`(divisionCode) → 공장목록` 캐시**로 쓴다.  
     같은 사업부 행 100개가 공장 셀을 열어도 API는 1번이다.
3. **실적 데이터는 연쇄마다 다시 조회하지 않는다.** 조회 / 페이지 / 엑셀 / 저장만.

그리드 셀 옵션을 매번 서버에서 받는 것은 비추천이다. 셀 에디터는 옵션이 바로 있어야 하고, 지연되면 “클릭했는데 반영 안 됨”과 구분이 안 된다.  
캐시된 마스터로 `getOptions(field, row)` 하는 쪽이 맞다.

이 데모의 **제품군**만 4.2를 화면으로 보여 준다. 조회조건 팝업과 그리드 셀 모두 `GET /api/product-groups?corp=&division=&plant=&line=&keyword=` 를 mock으로 친다. 그리드는 해당 행의 상위 코드로 조회하고 한 건만 고른다. 분류·규격·수율·리드타임·공정·월생산능력까지 같이 받는다. 법인·상태 같은 다른 그리드 셀은 여전히 `getOptions` Select다.

### 4.4 실적 조회와 섞지 말 것

- 조회조건 연쇄: 마스터만 걸러 Select 목록을 바꾼다. 실적 API를 치면 안 된다.
- 조회 버튼: 그때 `GET /inquiries?corp=&division=&page=`
- 그리드 셀 수정: 로컬 행만 바꾸고, **저장**에서 `PUT` / `PATCH`

연쇄 API를 실적 조회에 붙이면, 법인만 바꿔도 그리드가 다시 로드되어 편집 중인 행이 날아간다.

---

## 5. 회사 코드로 옮길 때 체크리스트

### 조회조건 Select `onChange`

1. 해당 필드 값만 갱신한다.
2. `childFieldsOf`로 자손 값을 새 옵션 기준으로 제거한다.
3. 각 Select의 `options={getOptions(...)}`가 새 부모를 보게 한다.

### 그리드 셀 Select

1. `onValueChange(code)` 후 `stopEditing()` 한다. (AG Grid 32)
2. `valueSetter`에서 `data[field] = newValue; return true` 한다.
3. `onCellValueChanged`에서 `cascadeRow` → `setData` → `refreshCells` 한다.
4. 옵션은 `params.data`(그 행)으로 계산한다.
5. Ant Design 드롭다운을 `body`로 빼면, 포커스 아웃으로 에디터가 닫히지 않게 처리한다.

### 마스터 API

- 코드가 작으면 진입 시 한 번 받는다.
- 크면 상위 변경 시 받되 `(부모코드 → 자식목록)`을 캐시한다.
- 그리드 셀마다 API를 치지 않는다.
- 실적 조회는 조회 / 페이징 / 엑셀 / 저장과만 연결한다.

---

## 6. 데이터 흐름 요약

```
[마스터 코드]
  화면 진입 시 1회 (또는 상위 변경 시 + 캐시)
  → catalogs
  → getOptions(field, 조회조건 or 행)

[조회조건 Select]
  onChange(code)
  → cascadeSearch
  → setSearch
  → 하위 Select options 재계산
  (실적 API 호출 없음)

[조회 / 페이징 / 엑셀]
  GET /inquiries (조건 + page)
  → 그리드 rowData

[그리드 셀 Select]
  onValueChange(code) → stopEditing
  → valueSetter
  → onCellValueChanged → cascadeRow → 그 행만 갱신
  (실적 API 호출 없음)

[저장]
  체크한 행의 변경 전/후 확인
  → PUT/PATCH
```
