// =============================================================
//  MASS CHART – FULL HISTORY + FETCH ON ZOOM
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
        .then(({ labels, values }) => initMassChart(canvas, labels, values));
});


// ------------------------------------------------------------
// FETCH MASS DATA
// ------------------------------------------------------------
async function fetchMassData(from, to) {
    const url = `/api/stocks/${massStockCode}/mass-window?from=${from}&to=${to}`;
    console.log("🌐 mass fetch:", url);

    const res = await fetch(url);
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
    const padding = (maxY - minY) * 0.1 || 1;

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
}


// ------------------------------------------------------------
// MASS – ZOOM HANDLER
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

    chart.data.labels = labels;
    chart.data.datasets[0].data = values;

    chart.update("none");
}
