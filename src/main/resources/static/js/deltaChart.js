document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('deltaChart');
    if (!canvas) return;

    const stockSelect = document.getElementById('stockSelect');
    const fromInput = document.getElementById('fromDate');
    const toInput = document.getElementById('toDate');

    let deltaChart;

    async function loadDelta() {
        const code = stockSelect?.value;
        if (!code) return;

        const from = fromInput?.value;
        const to = toInput?.value;

        const params = new URLSearchParams();
        if (from) params.append('from', from);
        if (to) params.append('to', to);

        const res = await fetch(`/api/stocks/${code}/delta-window?` + params.toString());
        if (!res.ok) return;

        const data = await res.json();

        const labels = data.map(p => p.date);
        const deltas = data.map(p => Number(p.delta || 0));
        const ema = data.map(p => Number(p.emaMomentum || 0));

        if (deltaChart) {
            deltaChart.destroy();
        }

        const ctx = canvas.getContext('2d');
        deltaChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Δ Price',
                        data: deltas,
                        borderWidth: 2,
                        tension: 0.2,
                        pointRadius: 0
                    },
                    {
                        label: 'EMA Momentum',
                        data: ema,
                        borderWidth: 2,
                        borderDash: [4, 4],
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
                    y: { title: { display: true, text: 'Δ / Momentum' } }
                }
            }
        });
    }

    stockSelect?.addEventListener('change', loadDelta);
    fromInput?.addEventListener('change', loadDelta);
    toInput?.addEventListener('change', loadDelta);

    loadDelta();
});
