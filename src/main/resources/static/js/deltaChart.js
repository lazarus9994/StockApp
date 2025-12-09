console.log("🔥 deltaChart.js loaded");

document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.getElementById("deltaChart");
    if (!canvas) return;

    let chart = null;

    async function loadDelta() {
        const code = canvas.dataset.code;
        if (!code) return;

        const { from, to } = getRange();

        const url = `/api/stocks/${code}/delta-window?from=${from}&to=${to}`;
        console.log("🌐 delta:", url);

        const res = await fetch(url);
        if (!res.ok) return;

        const data = await res.json();

        const labels = data.map(r => r.date);
        const d = data.map(r => Number(r.delta));

        if (chart) chart.destroy();

        chart = new Chart(canvas.getContext("2d"), {
            type: "line",
            data: {
                labels,
                datasets: [
                    { label: "Delta", data: d, borderWidth: 2 }
                ]
            },
            options: { responsive: true }
        });
    }

    window.addEventListener("stockChanged", loadDelta);
    loadDelta();
});
