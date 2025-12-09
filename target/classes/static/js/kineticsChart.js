console.log("🔥 kineticsChart.js loaded");

document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.getElementById("kineticsChart");
    if (!canvas) return;

    let chart = null;

    async function loadKinetics() {
        const code = canvas.dataset.code;
        if (!code) return;

        const { from, to } = getRange();

        const url = `/api/stocks/${code}/kinetics-window?from=${from}&to=${to}`;
        console.log("🌐 kinetics:", url);

        const res = await fetch(url);
        if (!res.ok) return;

        const data = await res.json();

        const labels = data.map(x => x.date);
        const v = data.map(x => Number(x.velocity));
        const a = data.map(x => Number(x.acceleration));

        if (chart) chart.destroy();

        chart = new Chart(canvas.getContext("2d"), {
            type: "line",
            data: {
                labels,
                datasets: [
                    { label: "Velocity", data: v, borderWidth: 2 },
                    { label: "Acceleration", data: a, borderWidth: 2 }
                ]
            },
            options: { responsive: true }
        });
    }

    window.addEventListener("stockChanged", loadKinetics);
    loadKinetics();
});
