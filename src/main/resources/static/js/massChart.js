console.log("🔥 massChart.js loaded");

document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.getElementById("massChart");
    if (!canvas) return;

    let chart = null;

    async function loadMass() {
        const code = canvas.dataset.code;
        if (!code) return;

        const { from, to } = getRange();

        const url = `/api/stocks/${code}/mass-window?from=${from}&to=${to}`;
        console.log("🌐 mass:", url);

        const res = await fetch(url);
        if (!res.ok) return;

        const data = await res.json();
        const labels = data.map(p => p.date);
        const values = data.map(p => Number(p.mass));

        if (chart) chart.destroy();

        chart = new Chart(canvas.getContext("2d"), {
            type: "line",
            data: {
                labels,
                datasets: [{ label: "Mass", data: values, borderWidth: 2 }]
            },
            options: { responsive: true }
        });
    }

    window.addEventListener("stockChanged", loadMass);
    loadMass();
});
