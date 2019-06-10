/*
   The client has to authorized first for below.
 */

/**
 * Print the summary and start datetime/date of the next ten events in
 * the authorized user's calendar. If no events are found an
 * appropriate message is printed.
 */
function fetchInstance(){
  gapi.client.calendar.calendarList.list({
    }).then(function(response) {
      var List = response.result.items;
      TimeLine.calendars = List.length;
      List.forEach((calendar) => {
        fetchEvents(calendar.id, calendar.summary);
      });
    });
}

function fetchEvents( ID, summary ){
  var dateBegin = new Date(), dateEnd = new Date();
  dateBegin.setHours(0,0,0,0);
  dateEnd.setHours(23, 59, 59, 999);
  gapi.client.calendar.events.list({
    'calendarId': ID,
    'timeMin': dateBegin.toISOString(),
    'timeMax': dateEnd.toISOString(),
    'showDeleted': false,
    'singleEvents': true,
    'orderBy': 'startTime'
    }).then(function(response) {
      var events = response.result.items;
      appendPre('Upcoming events in ' + summary + ' :');

      if (events.length > 0) {
        for (i = 0; i < events.length; i++) {
          var event = events[i];
          TimeLine.addEvent( summary, event );
          var when = event.start.dateTime;
          if (!when) {
            when = event.start.date;
          }
          appendPre(event.summary + ' (' + when + ')')
        }
      } else {
          appendPre('No upcoming events found.');
      }
      appendPre('-');
      TimeLine.checkCalendar();
    });
}

/**
 * Append a pre element to the body containing the given message
 * as its text node. Used to display the results of the API call.
 *
 * @param {string} message Text to be placed in pre element.
 */
function appendPre(message) {
  var pre = document.getElementById('content');
  var textContent = document.createTextNode(message + '\n');
  pre.appendChild(textContent);
}


function undoFetch(){
  var pre = document.getElementById('content');
  pre.innerHTML = '';
}

var TimeLine = {
  events: [],
  calendars: 0,
  checkCalendar: function(){
    this.calendars -= 1;
    if( this.calendars == 0 ) this.drawTimeLine();
  },
  addEvent: function( calendar, e ){
    if(e.start.dateTime){
      this.events.push( [calendar, e.summary, new Date(e.start.dateTime), new Date(e.end.dateTime) ]);
    }else{
      this.events.push( [calendar, e.summary, new Date(e.start.date), new Date(e.end.date) ]);
    }
  },
  drawTimeLine: function (){
    google.charts.load('current', {'packages':['timeline']});
    google.charts.setOnLoadCallback(drawChart);
    function drawChart() {
      var container = document.getElementById('timeline');
      var chart = new google.visualization.Timeline(container);
      var dataTable = new google.visualization.DataTable();

      dataTable.addColumn({ type: 'string', id: 'Calendar' });
      dataTable.addColumn({ type: 'string', id: 'Summary' });
      dataTable.addColumn({ type: 'date', id: 'Start' });
      dataTable.addColumn({ type: 'date', id: 'End' });

      dataTable.addRows(TimeLine.events);

      chart.draw(dataTable);
    }
  }

};
