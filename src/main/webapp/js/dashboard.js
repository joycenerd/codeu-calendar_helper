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
      var $timeTableContext = $("#timeTableContext")
                                .html("");
      events.forEach(( e ) => {
          if(e.start.dateTime != null){
            const eventDiv = buildTimetableEntry( e );
            $timeTableContext.append(eventDiv);
          }
          });
      });
}

function buildTimetableEntry( e ) {
  const options = { hour12: false, hour: "2-digit", minute: "2-digit" };
  const startTime = new Date(e.start.dateTime);
  var $start = $(document.createElement('div'))
                .addClass('row')
                .append( $(document.createElement('div'))
                          .addClass('col')
                          .text(startTime.toLocaleTimeString("default", options))
                        );

  var $end = $(document.createElement('div'))
              .addClass('row')
              .append( $(document.createElement('div'))
                          .addClass('col')
                          .text(endTime.toLocaleTimeString("default", options))
                      );

  var $time = $(document.createElement('div'))
                .addClass('col-3 pr-0 table-time')
                .append( $start )
                .append( $end );

  var $summary = $(document.createElement('div'))
                  .addClass('col text-turncate table-summary')
                  .text( e.summary );

  var $event = $(document.createElement('div'))
                  .addClass('row align-items-center mb-3 table-entry')
                  .append( $time )
                  .append( $summary );

  return $event;
}
