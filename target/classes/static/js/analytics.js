new Chart(document.getElementById("priceChart"), {
    type: "line",
    data: {
        labels: dates,
        datasets: [{
            label: "Close Price",
            data: closes,
            fill: false
        }]
    }
});

new Chart(document.getElementById("massChart"), {
    type: "line",
    data: {
        labels: massDates,
        datasets: [{
            label: "Mass",
            data: massValues,
            fill: false
        }]
    }
});

// DAILY
new Chart(document.getElementById("dailyChart"), {
    type: "line",
    data: {
        labels: daily.map(e => e.date),
        datasets: [{
            label: "Daily Frequency",
            data: daily.map(e => e.frequency),
            fill: false
        }]
    }
});

// WEEKLY
new Chart(document.getElementById("weeklyChart"), {
    type: "line",
    data: {
        labels: weekly.map(e => e.date),
        datasets: [{
            label: "Weekly Frequency",
            data: weekly.map(e => e.frequency),
            fill: false
        }]
    }
});

// MONTHLY
new Chart(document.getElementById("monthlyChart"), {
    type: "line",
    data: {
        labels: monthly.map(e => e.date),
        datasets: [{
            label: "Monthly Frequency",
            data: monthly.map(e => e.frequency),
            fill: false
        }]
    }
});

// YEARLY
new Chart(document.getElementById("yearlyChart"), {
    type: "line",
    data: {
        labels: yearly.map(e => e.date),
        datasets: [{
            label: "Yearly Frequency",
            data: yearly.map(e => e.frequency),
            fill: false
        }]
    }
});
