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
$(function() {  //$(document).ready is removed in 3.0

  //timeTable
  loadTimetable();

  $("#table-sample").popover({
        container: 'body',
        template: buildTableSampleTemplate(),
        html: true,
        title: '<input class="form-control form-control-lg" type="text" name="summary" placeholder="New Event" required />',
        content: buildTableSampleContent(),
        placement: 'auto',
        trigger: 'manual'
      }).toggleClass('d-none');

  bindingSamplePopover();

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
  
  //Biding on all selected elements including those elements that haven't been created yet.
  $( document ).on( "mousedown", ".popover:not(.table-form-popover)", function() {
      if( $('.popover').has(event.target).length == 0 ) return;
      event.preventDefault();
      });
});

function loadTimetable(){
  var start = new Date();
  var end = new Date(start);
  end.setDate( end.getDate() + 1 );
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
      var isAppendedNextDayBlock = false;
      events.forEach(( e ) => {
          if(e.start.dateTime != null){
            const $eventDiv = buildTimetableEntry( e );
            if( isAppendedNextDayBlock == false && new Date(e.start.dateTime).getDate() === end.getDate() ){
              var $NightNight = $( document.createElement('small') )
                                  .addClass( 'pl-1 night-night')
                                  .text( 'Night night~' );

              var $nextDayBlock = $( document.createElement('div') )
                                    .addClass( 'mb-1 next-day-block' )
                                    .append( $NightNight );
              $timeTableContext.append($nextDayBlock);
              isAppendedNextDayBlock = true;
            }
            $timeTableContext.append( $eventDiv );
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
  return ['<div class="container popover table-form-popover" role="tooltip">',
            '<div class="arrow"></div>',
            '<form id="table-form">',
            '<div class="row"><div class="popover-header col"></div></div>',
            '<div class="row"><div class="popover-body col"></div></div>',
            '</form>',
          '</div>'].join('');
}

function buildTableSampleContent(){
  return ['<label for="startDateTime">Start time:</label>',
          '<div class="row mb-1" id="startDateTime">',
              '<div class="col-7">',
                '<input type="number" class="year" required>', ' . ',
                '<input type="number" class="month" required>', ' . ',
                '<input type="number" class="date" step="1" required>',
              '</div>',
              '<div class="col">',
                '<input class="hours" type="number" min="0" max="23" step="1" required>',
                ' : ',
                '<input class="minutes" type="number" min="0" max="59" step="5" required>',
              '<input type="hidden" name="startDateTime">',
              '</div>',
          '</div>',
          '<label for="endDateTime">End time:</label>',
          '<div class="row mb-1" id="endDateTime">',
              '<div class="col-7">',
                '<input type="number" class="year" required>', ' . ',
                '<input type="number" class="month" required>', ' . ',
                '<input type="number" class="date" required />',
              '</div>',
              '<div class="col">',
                '<input type="number" class="hours" min="0" max="23" step="1" required>',
                ' : ',
                '<input type="number" class="minutes" min="0" max="59" step="5" required>',
              '<input type="hidden" name="endDateTime">',
              '</div>',
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
                '<input type="submit" class="btn btn-primary" id="table-submit" value="Store" tabindex="0">',
              '</div>',
            '</div>'].join('');
}

function initSamplePopover(){
  var now = new Date();
  var roundedMinutes = Math.ceil(now.getMinutes() / 5) * 5;
  now.setMinutes( roundedMinutes );
  var oneHourLater = new Date( now.getTime() );
  oneHourLater.setHours( now.getHours() + 1 );
  var oneDayLater = new Date( now.getTime() );
  oneDayLater.setDate( now.getDate() + 1 );

  const options = { hour12: false, hour: "2-digit", minute: "2-digit" };
  $('#table-sample-start').text( now.toLocaleTimeString( "default", options ));
  $('#table-sample-end').text( oneHourLater.toLocaleTimeString( "default", options ));
  $('#table-sample-summary').text('...');

  function initDateTime( $DateTime, min, max, value ){
    $DateTime.find( '.year' ).attr({'min': min.getFullYear(), 
                                    'max': max.getFullYear(),
                            }).val( value.getFullYear() );
    $DateTime.find( '.month' ).attr({'min': min.getMonth()+1, 
                                     'max': max.getMonth()+1,
                              }).val( value.getMonth()+1 );
    $DateTime.find( '.date' ).attr({'min': min.getDate(), 
                                     'max': max.getDate(),
                              }).val( value.getDate() );
    $DateTime.find( '.hours' ).val( value.getHours() );
    $DateTime.find( '.minutes' ).val( value.getMinutes() );
    $DateTime.find( '[type="hidden"]' ).val( value.toISOString() );
  
  }
  initDateTime( $( '#startDateTime' ) , now, oneDayLater, now );
  initDateTime( $( '#endDateTime' )   , now, oneDayLater, oneHourLater );
}

function bindingSamplePopover(){
  $( document ).on( 'focusout', '#table-form', function() {
      setTimeout( function(){
          if( $('#table-form input:focus, #table-form textarea:focus').length != 0 ) return;
          $('#table-sample').popover('hide');
          setTimeout( function(){
              $("#table-sample").toggleClass('d-none');
              }, 50);
          }, 0);
      });
  $( document ).on( 'change', '#table-form [name="summary"]', function() {
      if($(this).val().length == 0){
        $('#table-sample-summary').text( "..." );
      }else{
        $('#table-sample-summary').text( $(this).val() );
      }
      });
  //popover doesn't allow customized id, click is left mouse click and leave
  $( document ).on( 'mousedown', '.table-form-popover', function() {
        if( $('.table-form-popover').has(event.target).length == 0 ) return;
        if( event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA'){
          event.stopPropagation();
          return;
        };
        event.preventDefault();
      });
  $( document ).on( 'change', '#startDateTime input, #endDateTime input', function() {
      const $dateTime = $(this).parent().parent();
      var date = new Date(  parseInt($dateTime.find('input.year').val() ),
          parseInt($dateTime.find('input.month').val() ) - 1, //For date.getMonth() is 0-based
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
  $( document ).on( 'click', '#table-submit', function() {
      event.preventDefault();
      var eventData = $('#table-form').serializeArray();
      var startDateTime = new Date( eventData.find(o => o.name === 'startDateTime').value );
      var endDateTime   = new Date( eventData.find(o => o.name === 'endDateTime').value );
      if( startDateTime >= endDateTime ){
        alert("Invalid date time.");
        return;
      }
      $.post("/dashboard/calendar", eventData, function(data){
          if(data.error != null) window.location.replace(data.to);
          var tagData = {
            method: 'update',
            eventDateTime: eventData.find(o => o.name === 'startDateTime').value
          };
          const tags = eventData.find(o => o.name === 'tags').value;
          if(tags.length > 0){
            for( const tag of tags.split(/[\s,]+/) ){
              tagData.tag = tag;
              $.post("/dashboard/tagManage", tagData, function(response){
                  if(response.error != null) window.location.replace(data.to);
                  });
            };
          }
          $(document.activeElement).focusout();
          loadTimetable();    //Here should be modified to adding animatedly
          }, "json")
      .fail(function(err){
          console.log(err);
          });
      });
}
