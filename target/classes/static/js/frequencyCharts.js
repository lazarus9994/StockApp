// =============================================================
// FREQUENCY CHARTS – DAILY / WEEKLY / MONTHLY / YEARLY
// Full history + FETCH ON ZOOM + SYNC SLAVE
// =============================================================

console.log("🔥 frequencyCharts.js loaded");

const freqTypes = ["daily", "weekly", "monthly", "yearly"];

// държим тук chart + код + последен диапазон
const freqState = {
    // daily: { chart, code, lastFrom, lastTo }, ...
};

document.addEventListener("DOMContentLoaded", () => {

    freqTypes.forEach(type => {
        const canvas = document.getElementById(type + "Chart");
        if (!canvas) return;

        const code = canvas.dataset.code;
        if (!code) return;

        loadFrequency(code, type, "1900-01-01", "2100-01-01")
            .then(({ labels, values }) => {
                if (!labels.length) {
                    console.warn(`freq ${type}: no data`);
                    return;
                }
                initFrequencyChart(canvas, type, labels, values, code);
            })
            .catch(err => console.error(`freq ${type} init error:`, err));
    });

    // 🔗 слушаме синхронизация от priceChart
    window.addEventListener("syncRangeFromPrice", (e) => {
        const { from, to } = e.detail || {};
        if (!from || !to) return;

        freqTypes.forEach(type => syncFreqFromPrice(type, from, to));
    });
});

// ------------------------------------------------------------
// FETCH FREQUENCY DATA
// ------------------------------------------------------------
async function loadFrequency(code, type, from, to) {

    const url = `/api/stocks/${code}/frequency/${type}?from=${from}&to=${to}`;
    console.log(`🌐 freq ${type} fetch:`, url);

    const res = await fetch(url);
    if (!res.ok) {
        console.error(`❌ freq ${type} HTTP:`, res.status);
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
                label: type.toUpperCase() + " Frequency",
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

    freqState[type] = {
        chart,
        code,
        lastFrom: null,
        lastTo: null
    };

    console.log(`✅ ${type} chart initialized (${labels.length} points)`);
}

// ------------------------------------------------------------
// HANDLE ZOOM / PAN НА САМИЯ FREQ CHART
// ------------------------------------------------------------
async function handleFrequencyZoom(chart, type, code) {

    const st = freqState[type];
    if (!st) return;

    const x = chart.scales.x;
    const from = new Date(x.min).toISOString().slice(0, 10);
    const to   = new Date(x.max).toISOString().slice(0, 10);

    if (st.lastFrom === from && st.lastTo === to) return;

    st.lastFrom = from;
    st.lastTo = to;

    const { labels, values } = await loadFrequency(code, type, from, to);
    if (!labels.length) {
        console.warn(`freq ${type} zoom: no data`);
        return;
    }

    chart.data.labels = labels;
    chart.data.datasets[0].data = values;
    chart.update("none");
}

// ------------------------------------------------------------
// SYNC ОТ PRICE CHART → FREQUENCY CHART
// ------------------------------------------------------------
async function syncFreqFromPrice(type, from, to) {

    const st = freqState[type];
    if (!st || !st.chart) return;

    // ако сме вече на този диапазон → skip
    if (st.lastFrom === from && st.lastTo === to) return;

    st.lastFrom = from;
    st.lastTo = to;

    const { labels, values } = await loadFrequency(st.code, type, from, to);
    if (!labels.length) {
        console.warn(`freq ${type} sync: no data`);
        return;
    }

    st.chart.data.labels = labels;
    st.chart.data.datasets[0].data = values;
    st.chart.update("none");
}
