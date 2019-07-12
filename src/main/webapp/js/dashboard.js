// tag
$('#draggable-1').draggable({
  cursor: "move"
});
$('#draggable-2').draggable({
  cursor: "move"
});
$('#draggable-3').draggable({
  cursor: "move"
});
$('#draggable-4').draggable({
  cursor: "move"
});
$('#draggable-pointer').draggable({
  cursor: "pointer",
  cursorAt: { top: -5, left: -5 }
});
// today's tasks
$('#draggable-handle').draggable({
  handle: '.card-img-top'
});
// tag
var rowCount = 2;
var msg = '';
function addMoreRows(frm) {
    var recRow = '<p id="rowCount' + rowCount + '"><tr><td><input name="tagbox' + rowCount + '" id="tagbox' + rowCount + '" type="text" size="17%"  maxlength="120" /></td><td><input name="textbox' + rowCount + '" id="textbox' + rowCount + '" type="text"  maxlength="120" style="margin: 4px 5px 0 5px;"/></td></tr> <a href="javascript:void(0);" onclick="removeRow(' + rowCount + ');">Delete</a></p>';
    jQuery('#addedRows').append(recRow);
    rowCount++;
}

function removeRow(removeNum) {
  jQuery('#rowCount' + removeNum).remove();
}

$("#getButtonValue").click(function () {
    var msg = '';
    for (i = 1; i < rowCount; i++) {
        if ($('#tagbox' + i).val() == '' || $('#textbox' + i).val() == '') {
            continue;
        } else {
            msg += "\n #" + $('#tagbox' + i).val() + ":" + $('#textbox' + i).val();
        }

    }
    alert(msg);
    return msg;
});

// add items
$('#add-todo').click(function () {
    var lastSibling = $('#todo-list > .todo-wrap:last-of-type > input').attr('id');
    var newId;
    if (lastSibling == null) {
        newId = 1;
    }
    else {
        newId = Number(lastSibling) + 1;
    }
    $(this).before('<span class="editing todo-wrap"><input type="checkbox" id="' + newId + '"/><label for="' + newId + '" class="todo"><i class="fa fa-check"></i><input type="text" class="input-todo" id="input-todo' + newId + '"/></label></div>');
    $('#input-todo' + newId + '').parent().parent().animate({
        height: "36px"
    }, 200)
    $('#input-todo' + newId + '').focus();

    $('#input-todo' + newId + '').enterKey(function () {
        $(this).trigger('enterEvent');
    })

    $('#input-todo' + newId + '').on('blur enterEvent', function () {
        var todoTitle = $('#input-todo' + newId + '').val();
        var todoTitleLength = todoTitle.length;
        if (todoTitleLength > 0) {
            $(this).before(todoTitle);
            $(this).parent().parent().removeClass('editing');
            $(this).parent().after('<span class="delete-item" title="remove"><i class="fa fa-times-circle"></i></span>');
            $(this).remove();
            $('.delete-item').click(function () {
                var parentItem = $(this).parent();
                parentItem.animate({
                    left: "-30%",
                    height: 0,
                    opacity: 0
                }, 200);
                setTimeout(function () { $(parentItem).remove(); }, 1000);
            });
        }
        else {
            $('.editing').animate({
                height: '0px'
            }, 200);
            setTimeout(function () {
                $('.editing').remove()
            }, 400)
        }
    })

});

// remove items 

$('.delete-item').click(function () {
    var parentItem = $(this).parent();
    parentItem.animate({
        left: "-30%",
        height: 0,
        opacity: 0
    }, 200);
    setTimeout(function () { $(parentItem).remove(); }, 1000);
});

// Enter Key detect

$.fn.enterKey = function (fnc) {
    return this.each(function () {
        $(this).keypress(function (ev) {
            var keycode = (ev.keyCode ? ev.keyCode : ev.which);
            if (keycode == '13') {
                fnc.call(this, ev);
            }
        })
    })
}

// make task in today's task sortable
$(function () {
    $("#todo-list").sortable({
      placeholder: "ui-state-dragging"
    });
    $("#todo-list").disableSelection();
});


//JQuery
$(document).ready(function() { 

  //Timetable sumit
  $("#submit").click(function( event ){
      event.preventDefault();
      $.post("/dashboard/calendar", $("#timeTableForm").serializeArray(), function(events){
          if(events.error != null) window.location.replace(events.to);
        }, "json")
      .fail(function(err){
          console.log(err);
          });
      });
});

//timeTable
loadTimetable();

function loadTimetable(){
  var start = new Date();
  var end = new Date();
  end.setHours(23,59,59,999);
  const url = "/dashboard/calendar?from=dashboard.html&timeMin="+start.toISOString()+
              "&timeMax="+end.toISOString()+
              "&timezone="+Intl.DateTimeFormat().resolvedOptions().timeZone;
  var headers = new Headers({
      'isFetch': 'true'
      });
  fetch(url, {
        headers: headers
      })
    .then((response) => {
        return response.json();
        })
  .then((events) => {
      if(events.error != null) window.location.replace(events.to);  //calendar errors
      events = events.filter(function(value, index, events){
          return value.hasOwnProperty("start") && value.start.hasOwnProperty("dateTime");
      });
      events.sort(function(a,b){ 
      return new Date(a.start.dateTime) - new Date(b.start.dateTime)});
      const timeTableContext = document.getElementById('timeTableContext');
      while(timeTableContext.lastChild){
        timeTableContext.removeChild(timeTableContext.lastChild);
      }
      events.forEach(( e ) => {
          if(e.start.dateTime != null){
            const eventDiv = buildTimetableEntry( e );
            timeTableContext.appendChild(eventDiv);
          }
          });
      });
}

function buildTimetableEntry( e ) {
  const options = { hour12: false, hour: "2-digit", minute: "2-digit" };
  const startTime = new Date(e.start.dateTime);
  const startColDiv = document.createElement('div');
  startColDiv.classList.add('col');
  startColDiv.appendChild( document.createTextNode(startTime.toLocaleTimeString("default", options)) );
  const startDiv = document.createElement('div');
  startDiv.appendChild(startColDiv);
  startDiv.classList.add('row');

  const endTime = new Date(e.end.dateTime);
  const endColDiv = document.createElement('div');
  endColDiv.classList.add('col');
  endColDiv.appendChild( document.createTextNode(endTime.toLocaleTimeString("default", options)) );
  const endDiv = document.createElement('div');
  endDiv.appendChild(endColDiv);
  endDiv.classList.add('row');

  const timeDiv = document.createElement('div');
  timeDiv.classList.add('col-3', 'pr-0', 'table-time');
  timeDiv.appendChild( startDiv );
  timeDiv.appendChild( endDiv );

  const summDiv = document.createElement('div');
  summDiv.classList.add('col', 'text-turncate', 'table-summary');
  summDiv.appendChild( document.createTextNode(e.summary) );

  const eventDiv = document.createElement('div');
  eventDiv.classList.add('row', 'align-items-center', 'mb-3', 'table-entry');
  eventDiv.appendChild( timeDiv );
  eventDiv.appendChild( summDiv );

  return eventDiv;
}
