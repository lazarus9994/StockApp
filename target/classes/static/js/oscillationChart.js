console.log("🔥 oscillationChart.js loaded");

document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.getElementById("oscillationChart");
    if (!canvas) return;

    let chart = null;

    async function loadOscillation() {
        const code = canvas.dataset.code;
        if (!code) return;

        const { from, to } = getRange();

        const url = `/api/stocks/${code}/oscillation-window?from=${from}&to=${to}`;
        console.log("🌐 oscillation:", url);

        const res = await fetch(url);
        if (!res.ok) return;

        const data = await res.json();

        const labels = data.map(p => p.startDate);
        const r = data.map(p => Number(p.realFrequency || 0));
        const t = data.map(p => Number(p.theoreticalFrequency || 0));
        const g = data.map(p => Number(p.ratio || 0));

        if (chart) chart.destroy();

        chart = new Chart(canvas.getContext("2d"), {
            type: "line",
            data: {
                labels,
                datasets: [
                    { label: "Real", data: r, borderWidth: 2 },
                    { label: "Theoretical", data: t, borderWidth: 2 },
                    { label: "Ratio", data: g, borderWidth: 2, yAxisID: "y2" }
                ]
            },
            options: { responsive: true }
        });
    }

    window.addEventListener("stockChanged", loadOscillation);
    loadOscillation();
});
