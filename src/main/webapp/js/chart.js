function drawBarChart(){
  google.charts.load('current', {packages: ['corechart']});
  google.charts.setOnLoadCallback(drawChart);
  function drawChart(){
    fetch("/bookchart")
      .then((response) => {
        return response.json();
        })
      .then((bookJson) => {
        var book_data = new google.visualization.DataTable();
        //define columns for the DataTable instance
        book_data.addColumn('string', 'Book Title');
        book_data.addColumn('number', 'Rating');

        for (i = 0; i < bookJson.length; i++) {
          bookRow = [];
          var title = bookJson[i].title;
          var ratings = bookJson[i].rating;
          bookRow.push(title, ratings);

          book_data.addRow(bookRow);
        }
        var chart_options = {
          title: "Missing Book Names",
          width: 800,
          height: 400,
          fontSize: 11
        };
        var chart = new google.visualization.BarChart(document.getElementById('book_chart'));
        chart.draw(book_data);
        });
  }
}

function drawTimeLine(){
  google.charts.load('current', {'packages':['timeline']});
  google.charts.setOnLoadCallback(drawChart);
  function drawChart() {
    var container = document.getElementById('timeline');
    var chart = new google.visualization.Timeline(container);
    var dataTable = new google.visualization.DataTable();

    dataTable.addColumn({ type: 'string', id: 'President' });
    dataTable.addColumn({ type: 'date', id: 'Start' });
    dataTable.addColumn({ type: 'date', id: 'End' });
    dataTable.addRows([
      [ 'Adams',      new Date(1797, 2, 4),  new Date(1801, 2, 4) ],
      [ 'Washington', new Date(1789, 3, 30), new Date(1797, 2, 4) ],
      [ 'Jefferson',  new Date(1801, 2, 4),  new Date(1809, 2, 4) ]]);

    chart.draw(dataTable);
  }
}

function drawPieChart(){
  /*
   A problem with this chart that the pop-up caution will cause the js can't locate the mouse's cordinate quitely.
   */
  google.charts.load('current', {'packages':['corechart']});
  google.charts.setOnLoadCallback(drawChart);

  function drawChart() {

    var data = google.visualization.arrayToDataTable([
      ['Task', 'Hours per Day'],
      ['Work',     11],
      ['Eat',      2],
      ['Commute',  2],
      ['Watch TV', 2],
      ['Sleep',    7]
    ]);

    var options = {
      title: 'My Daily Activities'
    };

    var chart = new google.visualization.PieChart(document.getElementById('piechart'));

    chart.draw(data, options);
  }
}

drawBarChart();
drawTimeLine();
drawPieChart();
