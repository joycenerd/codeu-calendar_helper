//Global variables

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

  //timeTable
  loadTimetable();

  $("#table-sample").popover({
        template: buildTableSampleTemplate(),
        html: true,
        title: '<input type="text" name="summary" placeholder="New Event">',
        content: buildTableSampleContent(),
        placement: 'auto',
        trigger: 'manual'
      }).toggleClass('d-none');

  $("#timeTable").dblclick(function(){
      $("#table-sample").toggleClass('d-none');
      $('#table-sample').popover('show');
      $('[name="summary"]').focus();
      });

  $("#table-sample").on('shown.bs.popover', function(){
      $('#table-form').off('focusout')
                      .focusout(function(){
          $('#table-sample').popover('hide');
          setTimeout( function(){
              $("#table-sample").toggleClass('d-none');
              }, 50);
          });
      });
  
  $("#table-sample").on('hide.bs.popover', function(){
      });

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
  const endTime = new Date(e.end.dateTime);
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
                .addClass('col-4 pr-0 table-time')
                .append( $start )
                .append( $end );

  var $summary = $(document.createElement('div'))
                  .addClass('col text-turncate table-summary')
                  .text( e.summary );

  var $event = $(document.createElement('a'))
                  .attr("role", "button")
                  .attr("tabindex", "0")  //tabindex==0 so that this DOM will be focusable but not change the tab order
                  .addClass('row align-items-center p-1 m-1 mb-2 table-entry')
                  .append( $time )
                  .append( $summary )
                  .data( e );

  //Also useful in phon/email link
  var description = Autolinker.link(e.description.replace('#*', '') );

  var content = [ '<div class="row">',
                  '<div class="col">',
                  startTime.toLocaleTimeString("default"),
                  '</div><div class="col">',
                  endTime.toLocaleTimeString("default"),
                  '</div></div><div class="row"><div class="col">',
                  description,
                  '</div></div>'].join('');

  $event.popover({
        html: true,
        title: e.summary,
        content: content,
        placement: 'auto',
        trigger: 'focus'
      });

  return $event;
}

function buildTableSampleTemplate(){
  return ['<div class="container popover" role="tooltip">',
            '<div class="arrow"></div>',
            '<form id="table-form" class="container">',
            '<div class="row"><div class="popover-header col"></div></div>',
            '<div class="row"><div class="popover-body col"></div></div>',
            '</form>',
          '</div>'].join('');
}

function buildTableSampleContent(){
  return ['<div class="row">',
              '<input class="col" name="startDateTime">',
              '<span>~</span>',
              '<input class="col" name="endDateTime">',
            '</div>',
            '<div class="row">',
              '<textarea name="description" placeholder="description"></textarea>',
            '</div>',
            '<div class="row">',
              '<textarea name="tags" placeholder="tags"></textarea>',
            '</div>'].join('');
}
