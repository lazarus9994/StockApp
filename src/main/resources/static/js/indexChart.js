document.addEventListener("DOMContentLoaded", function () {

    const canvas = document.getElementById("marketChart");
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    let marketChart = null;

    const rangeSelect = document.getElementById("rangeSelect");

    // Определяме началния range (daily/weekly/monthly/yearly/all)
    const initialRange =
        new URLSearchParams(window.location.search).get("range") ||
        window.currentRange ||
        "monthly";

    loadChartData(initialRange);

    // При смяна на диапазон
    rangeSelect?.addEventListener("change", (e) => {
        const range = e.target.value;
        loadChartData(range);

        const params = new URLSearchParams(window.location.search);
        params.set("range", range);
        history.replaceState(null, "", "?" + params.toString());
    });

    // ======================================================
    //                 LOAD CHART DATA
    // ======================================================
    async function loadChartData(range) {
        try {
            console.log("📊 Loading NDAQ chart for range:", range);

            const res = await fetch(`/api/chart?range=${range}`);
            if (!res.ok) throw new Error("HTTP " + res.status);

            const records = await res.json();

            // Превръщаме данните
            const labels = records.map(r => new Date(r.date));
            const data = records.map(r => Number(r.close ?? 0));

            // Определяме timeUnit на база range
            const timeUnit = ({
                daily: "day",
                weekly: "week",
                monthly: "month",
                yearly: "year",
                all: "year"
            })[range] || "month";

            // Унищожаваме старата графика
            if (marketChart) {
                marketChart.destroy();
            }

            // ======================================================
            //                УНИФИЦИРАН Chart.js стил
            // ======================================================
            marketChart = new Chart(ctx, {
                type: "line",
                data: {
                    labels,
                    datasets: [{
                        label: "NDAQ Index",
                        data,
                        borderColor: "rgba(88,166,255,1)",
                        backgroundColor: "rgba(88,166,255,0.15)",
                        borderWidth: 2,
                        pointRadius: 0,
                        tension: 0.2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,

                    scales: {
                        x: {
                            type: "time",
                            time: { unit: timeUnit },
                            ticks: {
                                color: "#8b949e"
                            },
                            grid: {
                                color: "#21262d"
                            }
                        },
                        y: {
                            beginAtZero: false,
                            ticks: {
                                color: "#8b949e"
                            },
                            grid: {
                                color: "#21262d"
                            }
                        }
                    },

                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            callbacks: {
                                label: (ctx) =>
                                    `Close: $${ctx.parsed.y?.toFixed(2) ?? "N/A"}`
                            }
                        }
                    }
                }
            });

            console.log(`✅ Loaded ${records.length} NDAQ records (${range})`);

        } catch (err) {
            console.error("❌ NDAQ chart load failed:", err);
        }
    }
});
