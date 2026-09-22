$(document).ready(function() {
    // #statsChart에 심어둔 data-* 속성에서 데이터 가져오기 (없을 경우 기본값 0)
    var $chartEl = $('#statsChart');
    var todoCount = Number($chartEl.data('todo')) || 0;
    var doingCount = Number($chartEl.data('doing')) || 0;
    var doneCount = Number($chartEl.data('done')) || 0;

    // Highcharts 차트 생성
    Highcharts.chart('statsChart', {
        chart: { 
            type: 'pie' 
        },
        title: { 
            text: '할일 상태 비율' 
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.y}개</b> ({point.percentage:.1f}%)'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b>: {point.y}개'
                }
            }
        },
        series: [{
            name: '개수',
            data: [
                { name: '시작전', y: todoCount, color: '#6c757d' },
                { name: '진행중', y: doingCount, color: '#ffc107' },
                { name: '완료', y: doneCount, color: '#198754' }
            ]
        }]
    });
});