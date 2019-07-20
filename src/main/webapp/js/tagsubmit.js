$(document).ready(function () {
    $('#picker-no-time').dateTimePicker({ showTime: false, dateFormat: 'DD/MM/YYYY' }, "startDateTime");
    $('#picker').dateTimePicker(null, "endDateTime");
    $("#search-submit").click(function (event) {
        event.preventDefault();
        var eventData = $('#search-form').serializeArray();
        var startDateTime = new Date(eventData.find(o => o.name === 'startDateTime').value);
        var endDateTime = new Date(eventData.find(o => o.name === 'endDateTime').value);
        if (startDateTime >= endDateTime) {
            alert("Invalid date time.");
            return;
        }
        $.post("/dashboard/calendar", eventData, function (data) {
            console.log(data);
            if (data.error != null) window.location.replace(data.to);
            var tagData = {
                method: 'update',
                eventDateTime: eventData.find(o => o.name === 'startDateTime').value
            };
            for (const tag of eventData.find(o => o.name === 'tags').value.split(/[\s,]+/)) {
                tagData.tag = tag;
                console.log(tagData);
                $.post("/dashboard/tagManage", tagData, function (response) {
                    console.log(response);
                    if (response.error != null) window.location.replace(data.to);
                });
            };
            $('#search-form').focusout();
            loadTimetable();    //Here should be modified to adding animatedly
        }, "json")
            .fail(function (err) {
                console.log(err);
            });
    });
})