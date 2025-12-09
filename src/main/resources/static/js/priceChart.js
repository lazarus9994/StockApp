// =============================================================
//  PRICE CHART – FULL HISTORY + FETCH ON ZOOM + SYNC MASTER
// =============================================================

console.log("🔥 priceChart.js loaded");

let priceChart = null;
let stockCode = null;

let lastFetchMin = null;
let lastFetchMax = null;

// ------------------------------------------------------------
// DOM READY
// ------------------------------------------------------------
document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.getElementById("priceChart");
    if (!canvas) return;

    stockCode = canvas.dataset.code;
    if (!stockCode) return;

    // Load full history
    fetchAndUpdate("1900-01-01", "2100-01-01")
        .then(({ labels, values }) => {
            if (!labels.length) {
                console.warn("priceChart: no data");
                return;
            }
            initChart(canvas, labels, values);
        })
        .catch(err => console.error("priceChart init error:", err));
});

// ------------------------------------------------------------
// FETCH DATA FOR A TIME RANGE
// ------------------------------------------------------------
async function fetchAndUpdate(from, to) {

    const url = `/api/stocks/${stockCode}/records-window?from=${from}&to=${to}`;
    console.log("🌐 price fetch:", url);

    const res = await fetch(url);
    if (!res.ok) {
        console.error("❌ price HTTP error", res.status);
        return { labels: [], values: [] };
    }

    const data = await res.json();

    const labels = data.map(r => new Date(r.date + "T00:00:00"));
    const values = data.map(r => Number(r.price));

    console.log(`📦 price: ${labels.length} points`);
    return { labels, values };
}

// ------------------------------------------------------------
// INITIALIZE CHART
// ------------------------------------------------------------
function initChart(canvas, labels, values) {

    const ctx = canvas.getContext("2d");

    priceChart = new Chart(ctx, {
        type: "line",
        data: {
            labels,
            datasets: [{
                label: stockCode + " Close Price",
                data: values,
                borderColor: "rgba(88,166,255,1)",
                backgroundColor: "rgba(88,166,255,0.15)",
                borderWidth: 1.5,
                pointRadius: 0,
                tension: 0.15
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    type: "time",
                    time: { unit: "month" }
                }
            },
            plugins: {
                zoom: {
                    zoom: {
                        wheel: { enabled: true },
                        pinch: { enabled: true },
                        mode: "x",
                        onZoomComplete({ chart }) {
                            handlePriceZoom(chart);
                        }
                    },
                    pan: {
                        enabled: true,
                        mode: "x",
                        onPanComplete({ chart }) {
                            handlePriceZoom(chart);
                        }
                    }
                }
            }
        }
    });

    console.log("✅ priceChart initialized with", labels.length, "points");
}

// ------------------------------------------------------------
// HANDLE ZOOM / PAN (FETCH NEW DATA + BROADCAST RANGE)
// ------------------------------------------------------------
async function handlePriceZoom(chart) {

    const axis = chart.scales.x;
    const minDate = new Date(axis.min);
    const maxDate = new Date(axis.max);

    const from = minDate.toISOString().slice(0, 10);
    const to   = maxDate.toISOString().slice(0, 10);

    console.log("🔍 price zoomed range:", from, "→", to);

    // avoid duplicate fetch
    if (lastFetchMin === from && lastFetchMax === to) {
        console.log("⏳ price: skip fetch (same range)");
        return;
    }

    lastFetchMin = from;
    lastFetchMax = to;

    const { labels, values } = await fetchAndUpdate(from, to);
    if (!labels.length) {
        console.warn("⚠ price: no data in this range");
        return;
    }

    chart.data.labels = labels;
    chart.data.datasets[0].data = values;
    chart.update("none");

    // 🔔 СИНХРО ЗА ОСТАНАЛИТЕ ГРАФИКИ
    window.dispatchEvent(new CustomEvent("syncRangeFromPrice", {
        detail: { from, to }
    }));
}
