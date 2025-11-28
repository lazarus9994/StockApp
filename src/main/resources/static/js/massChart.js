// =============================================================
//  MASS CHART – FULL HISTORY + FETCH ON ZOOM + SYNC SLAVE
// =============================================================

console.log("🔥 massChart.js loaded");

let massChart = null;
let massStockCode = null;

let lastMassFetchMin = null;
let lastMassFetchMax = null;

// ------------------------------------------------------------
// DOM READY
// ------------------------------------------------------------
document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.getElementById("massChart");
    if (!canvas) return;

    massStockCode = canvas.dataset.code;
    if (!massStockCode) return;

    fetchMassData("1900-01-01", "2100-01-01")
        .then(({ labels, values }) => {
            if (!labels.length) {
                console.warn("massChart: no data");
                return;
            }
            initMassChart(canvas, labels, values);
        })
        .catch(err => console.error("mass init error:", err));
});

// ------------------------------------------------------------
// FETCH MASS DATA
// ------------------------------------------------------------
async function fetchMassData(from, to) {
    const url = `/api/stocks/${massStockCode}/mass-window?from=${from}&to=${to}`;
    console.log("🌐 mass fetch:", url);

    const res = await fetch(url);
    if (!res.ok) {
        console.error("❌ mass HTTP:", res.status);
        return { labels: [], values: [] };
    }

    const data = await res.json();

    return {
        labels: data.map(r => new Date(r.date + "T00:00:00")),
        values: data.map(r => Number(r.mass))
    };
}

// ------------------------------------------------------------
// INIT MASS CHART
// ------------------------------------------------------------
function initMassChart(canvas, labels, values) {

    const ctx = canvas.getContext("2d");

    const minY = Math.min(...values);
    const maxY = Math.max(...values);
    const padding = ((maxY - minY) || 1) * 0.1;

    massChart = new Chart(ctx, {
        type: "line",
        data: {
            labels,
            datasets: [{
                label: "Mass",
                data: values,
                borderColor: "orange",
                backgroundColor: "rgba(255,165,0,0.2)",
                borderWidth: 1.5,
                pointRadius: 0,
                tension: 0.15
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: { type: "time", time: { unit: "month" } },
                y: {
                    min: minY - padding,
                    max: maxY + padding
                }
            },
            plugins: {
                zoom: {
                    zoom: {
                        wheel: { enabled: true },
                        pinch: { enabled: true },
                        mode: "x",
                        onZoomComplete({ chart }) {
                            handleMassZoom(chart);
                        }
                    },
                    pan: {
                        enabled: true,
                        mode: "x",
                        onPanComplete({ chart }) {
                            handleMassZoom(chart);
                        }
                    }
                }
            }
        }
    });

    console.log("✅ massChart initialized:", labels.length, "points");

    // 🔗 регистрираме слушател за синхронизация от priceChart
    window.addEventListener("syncRangeFromPrice", async (e) => {
        const { from, to } = e.detail || {};
        if (!from || !to) return;

        // ако вече сме на този диапазон → skip
        if (from === lastMassFetchMin && to === lastMassFetchMax) return;

        lastMassFetchMin = from;
        lastMassFetchMax = to;

        const { labels: newLabels, values: newValues } = await fetchMassData(from, to);
        if (!newLabels.length) {
            console.warn("mass sync: no data");
            return;
        }

        const minY2 = Math.min(...newValues);
        const maxY2 = Math.max(...newValues);
        const pad2 = ((maxY2 - minY2) || 1) * 0.1;

        massChart.data.labels = newLabels;
        massChart.data.datasets[0].data = newValues;

        massChart.options.scales.y.min = minY2 - pad2;
        massChart.options.scales.y.max = maxY2 + pad2;

        massChart.update("none");
    });
}

// ------------------------------------------------------------
// MASS – ZOOM HANDLER (самостоятелно движение)
// ------------------------------------------------------------
async function handleMassZoom(chart) {

    const axis = chart.scales.x;
    const minDate = new Date(axis.min);
    const maxDate = new Date(axis.max);

    const from = minDate.toISOString().slice(0, 10);
    const to   = maxDate.toISOString().slice(0, 10);

    // prevent duplicate fetch
    if (from === lastMassFetchMin && to === lastMassFetchMax) return;
    lastMassFetchMin = from;
    lastMassFetchMax = to;

    const { labels, values } = await fetchMassData(from, to);
    if (!labels.length) {
        console.warn("mass zoom: no data");
        return;
    }

    const minY = Math.min(...values);
    const maxY = Math.max(...values);
    const padding = ((maxY - minY) || 1) * 0.1;

    chart.data.labels = labels;
    chart.data.datasets[0].data = values;

    chart.options.scales.y.min = minY - padding;
    chart.options.scales.y.max = maxY + padding;

    chart.update("none");
}
