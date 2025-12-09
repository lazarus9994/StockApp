console.log("🔥 analytics.js loaded");

// ========== DOM REFERENCES ==========
const stockSelect = document.getElementById("stockSelect");
const fromInput = document.getElementById("fromDate");
const toInput = document.getElementById("toDate");

// ========== RANGE HANDLER ==========
function getRange() {
    return {
        from: fromInput.value || "1900-01-01",
        to: toInput.value || "2100-01-01"
    };
}

// ========== UPDATE CANVAS DATA-CODE WHEN STOCK CHANGES ==========
function updateCanvasCodes(code) {
    document.querySelectorAll("canvas[data-code]").forEach(canvas => {
        canvas.dataset.code = code;
    });
}

// ========== TRIGGER ALL CHART RELOADERS ==========
function triggerReload() {
    window.dispatchEvent(new Event("stockChanged"));
}

// ========== EVENT LISTENERS ==========
stockSelect?.addEventListener("change", (e) => {
    const code = e.target.value;
    if (!code) return;

    updateCanvasCodes(code);
    triggerReload();
});

fromInput?.addEventListener("change", triggerReload);
toInput?.addEventListener("change", triggerReload);

// Initial load
triggerReload();
