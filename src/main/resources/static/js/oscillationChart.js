document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('oscillationChart');
    if (!canvas) return;

    const stockSelect = document.getElementById('stockSelect');
    const fromInput = document.getElementById('fromDate');
    const toInput = document.getElementById('toDate');

    let oscChart;

    async function loadOscillation() {
        const code = stockSelect?.value;
        if (!code) return;

        const from = fromInput?.value;
        const to = toInput?.value;

        const params = new URLSearchParams();
        if (from) params.append('from', from);
        if (to) params.append('to', to);

        const res = await fetch(`/api/stocks/${code}/oscillation-window?` + params.toString());
        if (!res.ok) return;

        const data = await res.json();

        const labels = data.map(p => p.startDate); // можем да ползваме startDate като ос
        const realFreq = data.map(p => Number(p.realFrequency || 0));
        const theorFreq = data.map(p => Number(p.theoreticalFrequency || 0));
        const ratio = data.map(p => Number(p.ratio || 0));

        if (oscChart) {
            oscChart.destroy();
        }

        const ctx = canvas.getContext('2d');
        oscChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Real frequency',
                        data: realFreq,
                        borderWidth: 2,
                        tension: 0.2,
                        pointRadius: 0
                    },
                    {
                        label: 'Theoretical frequency',
                        data: theorFreq,
                        borderWidth: 2,
                        borderDash: [4, 4],
                        tension: 0.2,
                        pointRadius: 0
                    },
                    {
                        label: 'Ratio f_real / f_theor',
                        data: ratio,
                        borderWidth: 1,
                        borderDash: [2, 2],
                        tension: 0.2,
                        pointRadius: 0,
                        yAxisID: 'y2'
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
                    x: { title: { display: true, text: 'Period start' } },
                    y: { title: { display: true, text: 'Frequency' } },
                    y2: {
                        position: 'right',
                        title: { display: true, text: 'Ratio' },
                        grid: { drawOnChartArea: false }
                    }
                }
            }
        });
    }

    stockSelect?.addEventListener('change', loadOscillation);
    fromInput?.addEventListener('change', loadOscillation);
    toInput?.addEventListener('change', loadOscillation);

    loadOscillation();
});
