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
        title: '<input class="form-control form-control-lg" type="text" name="summary" placeholder="New Event" required />',
        content: buildTableSampleContent(),
        placement: 'auto',
        trigger: 'manual'
      }).toggleClass('d-none');

  $("#timeTable").dblclick(function(){
      $("#table-sample").toggleClass('d-none');
      $('#table-sample').popover('show');
      $('[name="summary"]').focus();
      });

  $("#table-sample").on('hide.bs.popover', function(){
      $('#table-form').off('focusout');
      });

  $("#table-sample").on('show.bs.popover', function(){
      setTimeout( function(){
          initSamplePopover();
      }, 0);
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
  if( e.start.dateTime.value == undefined ){
    var startTime = new Date(e.start.dateTime);
    var endTime = new Date(e.end.dateTime);
  }else{
    var startTime = new Date(e.start.dateTime.value);
    var endTime = new Date(e.end.dateTime.value);
  }
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
  var description = "";
  if(e.description != undefined) description = Autolinker.link(e.description.replace('#*', '') );

  var content = [ '<div class="row">',
                  '<div class="col">',
                  startTime.toLocaleTimeString("default"),
                  '</div><span>~</span><div class="col">',
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
            '<form id="table-form">',
            '<div class="row"><div class="popover-header col"></div></div>',
            '<div class="row"><div class="popover-body col"></div></div>',
            '</form>',
          '</div>'].join('');
}

function buildTableSampleContent(){
  var now = new Date();
  return ['<div class="row" id="startDateTime">',
              '<input class="form-control year col" type="text" value="',now.getFullYear(),'" required />',
              '<span>/</span>',
              '<input class="form-control month col" type="text" value="',now.getMonth(),'" required />',
              '<span>/</span>',
              '<input class="form-control date col" type="text" value="',now.getDate(),'" required />',
              '<input class="form-control hours col" type="text" value="',now.getHours(),'" required />',
              '<input class="form-control minutes col" type="text" value="',now.getMinutes(),'" required />',
              '<input type="hidden" name="startDateTime" value="',now.toISOString(),'">',
            '</div><div class="row" id="endDateTime">',
              '<input class="form-control year col" type="text" value="',now.getFullYear(),'" required />',
              '<span>/</span>',
              '<input class="form-control month col" type="text" value="',now.getMonth(),'" required />',
              '<span>/</span>',
              '<input class="form-control date col" type="text" value="',now.getDate(),'" required />',
              '<input class="form-control hours col" type="text" value="',now.getHours(),'" required />',
              '<input class="form-control minutes col" type="text" value="',now.getMinutes(),'" required />',
              '<input type="hidden" name="endDateTime" value="',now.toISOString(),'">',
            '</div>',
            '<div class="row">',
              '<div class="form-group col">',
                '<label for="table-form-desc">Description</label>',
                '<textarea class="form-control" name="description" id="table-form-desc" placeholder="description"></textarea>',
              '</div>',
            '</div>',
            '<div class="row">',
              '<div class="form-group col">',
                '<label for="table-form-tags">Tags</label>',
                '<input class="form-control" type="text" name="tags" id="table-form-tags" placeholder="tags">',
              '</div>',
            '</div>',
            '<div class="row">',
              '<div class="col">',
                '<input type="submit" class="btn btn-primary" id="table-submit" tabindex="0">Store</button>',
              '</div>',
            '</div>'].join('');
}

function initSamplePopover(){
  var now = new Date();
  const options = { hour12: false, hour: "2-digit", minute: "2-digit" };
  $('#table-sample-start').text( now.toLocaleTimeString( "default", options ));
  $('#table-sample-end').text(now.toLocaleTimeString( "default", options ));
  $('#table-sample-summary').text('...');

  $('#table-form').focusout(function(){
      setTimeout( function(){
          if( $('#table-form input:focus, #table-form textarea:focus').length != 0 ) return;
          $('#table-sample').popover('hide');
          setTimeout( function(){
              $("#table-sample").toggleClass('d-none');
              }, 50);
          }, 0);
      });
  $('#table-form [name="summary"]').change(function(){
      $('#table-sample-summary').text( $(this).val() );
      });
  $('#startDateTime input, #endDateTime input').change(function(){
      const $dateTime = $(this).parent();
      var date = new Date(  parseInt($dateTime.find('input.year').val() ),
          parseInt($dateTime.find('input.month').val() ),
          parseInt($dateTime.find('input.date').val() ),
          parseInt($dateTime.find('input.hours').val() ),
          parseInt($dateTime.find('input.minutes').val() ),
          );
      if( isNaN(date) ){
        if($dateTime.attr('id')[0] == 's'){
          $('#table-sample-start').text( "" );
        }
        else {
          $('#table-sample-start').text( "" );
        }
      return;
      }
      $dateTime.find('input[type="hidden"]').attr( 'value', date.toISOString() );
      const options = { hour12: false, hour: "2-digit", minute: "2-digit" };
      if($dateTime.attr('id')[0] == 's'){
        $('#table-sample-start').text( date.toLocaleTimeString("default", options) );
      }
      else{
        $('#table-sample-end').text( date.toLocaleTimeString("default", options) );
      }
  });
}