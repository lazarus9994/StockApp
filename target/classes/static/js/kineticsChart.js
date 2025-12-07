document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('kineticsChart');
    if (!canvas) return;

    const stockSelect = document.getElementById('stockSelect');
    const fromInput = document.getElementById('fromDate');
    const toInput = document.getElementById('toDate');

    let kineticsChart;

    async function loadKinetics() {
        const code = stockSelect?.value;
        if (!code) return;

        const from = fromInput?.value;
        const to = toInput?.value;

        const params = new URLSearchParams();
        if (from) params.append('from', from);
        if (to) params.append('to', to);

        const res = await fetch(`/api/stocks/${code}/kinetics-window?` + params.toString());
        if (!res.ok) return;

        const data = await res.json();

        const labels = data.map(p => p.date);
        const velocity = data.map(p => Number(p.velocity || 0));
        const acceleration = data.map(p => Number(p.acceleration || 0));
        const force = data.map(p => Number(p.netForce || 0));

        if (kineticsChart) {
            kineticsChart.destroy();
        }

        const ctx = canvas.getContext('2d');
        kineticsChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Velocity',
                        data: velocity,
                        borderWidth: 2,
                        tension: 0.2,
                        pointRadius: 0
                    },
                    {
                        label: 'Acceleration',
                        data: acceleration,
                        borderWidth: 2,
                        borderDash: [4, 4],
                        tension: 0.2,
                        pointRadius: 0
                    },
                    {
                        label: 'Force (Δmv)',
                        data: force,
                        borderWidth: 1,
                        borderDash: [2, 2],
                        tension: 0.2,
                        pointRadius: 0
                    }
                ]
            },
            options: {
                responsive: true,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                scales: {
                    x: { title: { display: true, text: 'Date' } },
                    y: { title: { display: true, text: 'Value' } }
                }
            }
        });
    }

    stockSelect?.addEventListener('change', loadKinetics);
    fromInput?.addEventListener('change', loadKinetics);
    toInput?.addEventListener('change', loadKinetics);

    loadKinetics();
});
