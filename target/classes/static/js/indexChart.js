document.addEventListener("DOMContentLoaded", function () {

    const ctx = document.getElementById("marketChart")?.getContext("2d");
    if (!ctx) return;

    let marketChart = null;

    const rangeSelect = document.getElementById("rangeSelect");

    // ако Thymeleaf не подаде range → fallback monthly
    const initialRange =
        new URLSearchParams(window.location.search).get("range") ||
        window.currentRange ||
        "monthly";

    loadChartData(initialRange);

    // слушаме промяна
    rangeSelect?.addEventListener("change", (e) => {
        const range = e.target.value;
        loadChartData(range);

        const params = new URLSearchParams(window.location.search);
        params.set("range", range);
        history.replaceState(null, "", "?" + params.toString());
    });

    // ======================================================
    //            LOAD CHART DATA
    // ======================================================
    async function loadChartData(range) {
        try {
            console.log("🔄 Loading chart for", range);

            const res = await fetch(`/api/chart?range=${range}`);
            if (!res.ok) throw new Error("HTTP " + res.status);

            const records = await res.json();

            const labels = records.map(r => new Date(r.date));
            const data = records.map(r => Number(r.close ?? 0));

            const timeUnit = ({
                daily: "day",
                weekly: "week",
                monthly: "month",
                yearly: "year",
                all: "year"
            })[range] || "month";

            // унищожаваме стара графика
            if (marketChart && typeof marketChart.destroy === "function") {
                marketChart.destroy();
            }

            marketChart = new Chart(ctx, {
                type: "line",
                data: {
                    labels,
                    datasets: [{
                        label: "NDAQ",
                        data,
                        borderColor: "#58a6ff",
                        backgroundColor: "rgba(88,166,255,0.1)",
                        tension: 0.3,
                        pointRadius: 0,
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: {
                            type: "time",
                            time: { unit: timeUnit },
                            ticks: { color: "#8b949e" },
                            grid: { color: "#21262d" }
                        },
                        y: {
                            beginAtZero: true,
                            ticks: { color: "#8b949e" },
                            grid: { color: "#21262d" }
                        }
                    },
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                label: ctx =>
                                    `Close: $${ctx.parsed.y?.toFixed(2) ?? "N/A"}`
                            }
                        }
                    }
                }
            });

            console.log(`📊 Loaded ${records.length} records for NDAQ (${range})`);

        } catch (err) {
            console.error("❌ Chart load failed:", err);
        }
    }
});
