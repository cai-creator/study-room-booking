(function() {
  var style = getComputedStyle(document.documentElement);
  var accent = style.getPropertyValue('--accent').trim();
  var accent2 = style.getPropertyValue('--accent2').trim();
  var ink = style.getPropertyValue('--ink').trim();
  var muted = style.getPropertyValue('--muted').trim();
  var rule = style.getPropertyValue('--rule').trim();
  var bg2 = style.getPropertyValue('--bg2').trim();

  // --- Chart: 各自习室日均使用率对比 ---
  var chartUsage = echarts.init(document.getElementById('chart-usage'), null, { renderer: 'svg' });
  chartUsage.setOption({
    animation: false,
    tooltip: {
      trigger: 'axis',
      appendToBody: true,
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['图书馆-A区', '图书馆-B区', '图书馆-C区', '教三-201', '教三-202', '教四-101', '教四-102', '综教-501'],
      axisLine: { lineStyle: { color: rule } },
      axisLabel: { color: ink, rotate: 20, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: rule, type: 'dashed' } },
      axisLabel: { color: muted, formatter: '{value}%' }
    },
    series: [{
      type: 'bar',
      barWidth: '50%',
      data: [
        { value: 92, itemStyle: { color: accent } },
        { value: 85, itemStyle: { color: accent } },
        { value: 78, itemStyle: { color: accent } },
        { value: 65, itemStyle: { color: accent2 } },
        { value: 58, itemStyle: { color: accent2 } },
        { value: 72, itemStyle: { color: accent2 } },
        { value: 45, itemStyle: { color: accent + '99' } },
        { value: 38, itemStyle: { color: accent + '99' } }
      ],
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%',
        color: ink,
        fontSize: 11
      }
    }]
  });
  window.addEventListener('resize', function() { chartUsage.resize(); });

  // --- Chart: 24小时时段占用热力分布 ---
  var chartHeatmap = echarts.init(document.getElementById('chart-heatmap'), null, { renderer: 'svg' });

  var hours = ['08:00', '09:00', '10:00', '11:00', '12:00',
               '13:00', '14:00', '15:00', '16:00', '17:00',
               '18:00', '19:00', '20:00', '21:00', '22:00'];
  var rooms = ['图书馆-A区', '图书馆-B区', '教三-201', '教三-202', '教四-101', '教四-102'];

  var data = [
    [0,0,15],[0,1,22],[0,2,35],[0,3,48],[0,4,42],[0,5,38],[0,6,55],[0,7,68],[0,8,72],[0,9,65],[0,10,58],[0,11,82],[0,12,90],[0,13,88],[0,14,75],
    [1,0,18],[1,1,25],[1,2,38],[1,3,45],[1,4,40],[1,5,35],[1,6,52],[1,7,62],[1,8,68],[1,9,60],[1,10,55],[1,11,78],[1,12,85],[1,13,82],[1,14,70],
    [2,0,10],[2,1,15],[2,2,28],[2,3,35],[2,4,30],[2,5,25],[2,6,40],[2,7,50],[2,8,55],[2,9,48],[2,10,42],[2,11,60],[2,12,68],[2,13,65],[2,14,55],
    [3,0,8],[3,1,12],[3,2,22],[3,3,30],[3,4,25],[3,5,20],[3,6,35],[3,7,45],[3,8,50],[3,9,42],[3,10,38],[3,11,55],[3,12,62],[3,13,60],[3,14,50],
    [4,0,12],[4,1,18],[4,2,30],[4,3,38],[4,4,35],[4,5,30],[4,6,48],[4,7,58],[4,8,62],[4,9,55],[4,10,50],[4,11,72],[4,12,80],[4,13,78],[4,14,68],
    [5,0,5],[5,1,8],[5,2,15],[5,3,20],[5,4,18],[5,5,15],[5,6,25],[5,7,32],[5,8,35],[5,9,30],[5,10,28],[5,11,40],[5,12,48],[5,13,45],[5,14,38]
  ];

  // Fill missing cells with '-'
  var fullData = data.slice();
  for (var yi = 0; yi < rooms.length; yi++) {
    for (var xi = 0; xi < hours.length; xi++) {
      var exists = data.some(function(d) { return d[0] === xi && d[1] === yi; });
      if (!exists) {
        fullData.push([xi, yi, '-']);
      }
    }
  }

  chartHeatmap.setOption({
    animation: false,
    tooltip: {
      appendToBody: true,
      formatter: function(p) {
        if (p.value[2] === '-') return p.name + ': N/A';
        return rooms[p.value[1]] + '<br/>' + hours[p.value[0]] + ': ' + p.value[2] + '%';
      }
    },
    grid: {
      left: '12%',
      right: '8%',
      bottom: '12%',
      top: '5%'
    },
    xAxis: {
      type: 'category',
      data: hours,
      splitArea: { show: false },
      axisLine: { lineStyle: { color: rule } },
      axisLabel: { color: ink, fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: rooms,
      splitArea: { show: false },
      axisLine: { lineStyle: { color: rule } },
      axisLabel: { color: ink, fontSize: 11 }
    },
    visualMap: {
      min: 0,
      max: 100,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '0%',
      inRange: {
        color: [bg2, accent2, accent]
      },
      outOfRange: {
        color: 'transparent'
      },
      textStyle: { color: muted }
    },
    series: [{
      type: 'heatmap',
      data: fullData,
      label: {
        show: true,
        formatter: function(p) {
          if (p.value[2] === '-') return 'N/A';
          return p.value[2] + '%';
        },
        color: ink,
        fontSize: 10
      }
    }]
  });
  window.addEventListener('resize', function() { chartHeatmap.resize(); });
})();
