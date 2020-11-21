class DiagramController {
    
    constructor(diagramDomId, dataCache) {
        var context = $('#'+diagramDomId).get(0).getContext('2d');
        this.chart = this.createChart(context);
        this.dataCache = dataCache;
    }

    onRangeChanged(newRange) {
        this.range = newRange;
    }

    updateChart() {
        const valuesInRange = this.dataCache.getKeyValueMapInRange(this.range);
        this.chart.data.datasets.forEach((dataset) => {
            if(valuesInRange.has(dataset.id)) {
                dataset.data = valuesInRange.get(dataset.id);
            }
        });
        this.chart.update();
    }

    createChart(context) {
        return new Chart(context, {
            type: 'scatter',
            data: {
                datasets: [{
                    id: 'outside-temperature',
                    label: 'Temperatur',
                    yAxisID: 'temperature',
                    borderColor: '#ef5350',
                    fill: false,
                    showLine: true,
                    data: []
                }, {
                    id: 'water-lvl-distance',
                    label: 'Wasser',
                    yAxisID: 'water',
                    borderColor: '#42a5f5',
                    showLine: true,
                    fill: false,
                    data: []
                }],
            },
            options: {
                legend: {
                    position: 'bottom',
                },
                scales: {
                    xAxes: [{
                        type: 'time',
                        time: {
                            stepSize: 4,
                            displayFormats: {
                                hour: 'HH:mm DD.MMM',
                                day: 'DD.MM.YYYY',
                            },
                            isoWeekday: true,
                            tooltipFormat: 'DD.MM.YYYY HH:mm:ss'
                        }
                    }],
                    yAxes: [{
                        id: 'temperature',
                        position: 'left',
                        scaleLabel: {
                            display: true,
                            labelString: 'Temperatur (°C)',
                        }
                    }, {
                        id: 'water',
                        position: 'left',
                        scaleLabel: {
                            display: true,
                            labelString: 'Wasser (l)',
                        }
                    }]
                },
                animation: {
                    duration: 0
                }
            }
        });
    }
}