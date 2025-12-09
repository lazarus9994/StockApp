console.log("🔥 frequencyCharts.js loaded");

document.addEventListener("DOMContentLoaded", () => {
    const charts = {
        daily: { id: "dailyChart", instance: null, endpoint: "daily" },
        weekly: { id: "weeklyChart", instance: null, endpoint: "weekly" },
        monthly: { id: "monthlyChart", instance: null, endpoint: "monthly" },
        yearly: { id: "yearlyChart", instance: null, endpoint: "yearly" }
    };

    async function loadFrequency(type) {
        const item = charts[type];
        const canvas = document.getElementById(item.id);
        if (!canvas) return;

        const code = canvas.dataset.code;
        if (!code) return;

        const { from, to } = getRange();

        const url = `/api/stocks/${code}/frequency/${item.endpoint}?from=${from}&to=${to}`;
        console.log("🌐 frequency fetch:", url);

        const res = await fetch(url);
        if (!res.ok) return;

        const data = await res.json();
        const labels = data.map(r => new Date(r.date + "T00:00:00"));
        const values = data.map(r => Number(r.frequency));

        const ctx = canvas.getContext("2d");

        if (item.instance) item.instance.destroy();

        item.instance = new Chart(ctx, {
            type: "line",
            data: {
                labels,
                datasets: [{
                    label: `${type.toUpperCase()} Frequency`,
                    data: values,
                    borderWidth: 2,
                    tension: 0.2,
                    pointRadius: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { type: "time", time: { unit: "month" } }
                }
            }
        });
    }

    function reloadAll() {
        loadFrequency("daily");
        loadFrequency("weekly");
        loadFrequency("monthly");
        loadFrequency("yearly");
    }

    window.addEventListener("stockChanged", reloadAll);
    reloadAll();
});
