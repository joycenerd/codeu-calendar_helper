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
            var text = $("<p>You have just posted tag on your calendar,you can check it by opening your calendar or just close this window.</p>");
            var block = $('<div id="dialog" title="Successfully adding tag!" ><div>');
            var r = $('<input type="button" value="confirm?" id="opener" class="button" />');
            $(".card-body-tag").append(r);
            $(".card-body-tag").append(block);
            $("#dialog").dialog({
                autoOpen: false,
                show: {
                    effect: "blind",
                    duration: 1000
                },
                hide: {
                    effect: "explode",
                    duration: 1000
                }
            });
            $("#opener").on("click", function () {
                $("#dialog").dialog("open");
                $("#dialog").append(text);
                setTimeout(function () { // prevent creating too many buttons
                    $("#opener").remove();
                }, 5000);
                setTimeout(function () {
                    $("#dialog").remove();
                }, 20000);
            });
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
        }, "json")
            .fail(function (err) {
                console.log(err);
            });
    });

})