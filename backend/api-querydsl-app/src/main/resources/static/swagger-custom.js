(function () {
  var didInitialOpen = false;

  function addHeads() {
    document.querySelectorAll("section.models table.model").forEach(function (table) {
      if (table.dataset.tableHead === "1") {
        return;
      }
      table.dataset.tableHead = "1";
      if (table.querySelector("thead")) {
        return;
      }
      var thead = document.createElement("thead");
      thead.innerHTML = "<tr><th>필드</th><th>타입 — 설명</th></tr>";
      table.insertBefore(thead, table.firstChild);
    });
  }

  function openOnce() {
    if (didInitialOpen) {
      return;
    }
    var models = document.querySelectorAll("section.models .model-container");
    if (models.length === 0) {
      return;
    }
    models.forEach(function (box) {
      var btn = box.querySelector("button[aria-expanded='false'], .model-box-control[aria-expanded='false']");
      if (btn) {
        btn.click();
      }
    });
    didInitialOpen = true;
  }

  function tick() {
    openOnce();
    addHeads();
    if (didInitialOpen) {
      clearInterval(timer);
    }
  }

  var timer = setInterval(tick, 400);
  window.addEventListener("load", function () {
    setTimeout(tick, 400);
  });
})();
