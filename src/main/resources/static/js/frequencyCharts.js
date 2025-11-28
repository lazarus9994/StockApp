// =============================================================
// FREQUENCY CHARTS – DAILY / WEEKLY / MONTHLY / YEARLY (ZOOM + FETCH)
// =============================================================

console.log("🔥 frequencyCharts.js loaded");

const freqTypes = ["daily", "weekly", "monthly", "yearly"];
const freqCharts = {};
const lastFreqRange = {};

document.addEventListener("DOMContentLoaded", () => {

    freqTypes.forEach(type => {
        const canvas = document.getElementById(type + "Chart");
        if (!canvas) return;

        const code = canvas.dataset.code;

        loadFrequency(code, type, "1900-01-01", "2100-01-01")
            .then(({ labels, values }) => initFrequencyChart(canvas, type, labels, values, code));
    });

});

// ------------------------------------------------------------
// FETCH FREQUENCY DATA
// ------------------------------------------------------------
async function loadFrequency(code, type, from, to) {

    const url = `/api/stocks/${code}/frequency/${type}?from=${from}&to=${to}`;
    console.log("🌐 freq fetch:", url);

    const res = await fetch(url);
    if (!res.ok) {
        console.error("❌ freq HTTP:", res.status);
        return { labels: [], values: [] };
    }

    const data = await res.json();

    return {
        labels: data.map(r => new Date(r.date + "T00:00:00")),
        values: data.map(r => Number(r.frequency))
    };
}

// ------------------------------------------------------------
// INIT FREQUENCY CHART
// ------------------------------------------------------------
function initFrequencyChart(canvas, type, labels, values, code) {

    const ctx = canvas.getContext("2d");

    const chart = new Chart(ctx, {
        type: "line",
        data: {
            labels,
            datasets: [{
                label: type.toUpperCase(),
                data: values,
                borderColor: "green",
                backgroundColor: "rgba(0,255,0,0.1)",
                borderWidth: 1.3,
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
                            handleFrequencyZoom(chart, type, code);
                        }
                    },
                    pan: {
                        enabled: true,
                        mode: "x",
                        onPanComplete({ chart }) {
                            handleFrequencyZoom(chart, type, code);
                        }
                    }
                }
            }
        }
    });

    freqCharts[type] = chart;
    lastFreqRange[type] = { from: null, to: null };

    console.log(`✅ ${type} chart initialized (${labels.length} points)`);
}

// ------------------------------------------------------------
// HANDLE ZOOM → FETCH NEW RANGE
// ------------------------------------------------------------
async function handleFrequencyZoom(chart, type, code) {

    const x = chart.scales.x;

    const from = new Date(x.min).toISOString().slice(0, 10);
    const to   = new Date(x.max).toISOString().slice(0, 10);

    if (lastFreqRange[type]?.from === from &&
        lastFreqRange[type]?.to === to) return;

    lastFreqRange[type] = { from, to };

    const { labels, values } = await loadFrequency(code, type, from, to);

    if (!labels.length) return;

    chart.data.labels = labels;
    chart.data.datasets[0].data = values;
    chart.update("none");
}
