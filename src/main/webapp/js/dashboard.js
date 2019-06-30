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
var rowCount = 1;
function addMoreRows(frm) {
    rowCount++;
    var recRow = '<p id="rowCount' + rowCount + '"><tr><td><input name="" type="text" size="17%"  maxlength="120" /></td><td><input name="" type="text"  maxlength="120" style="margin: 4px 5px 0 5px;"/></td></tr> <a href="javascript:void(0);" onclick="removeRow(' + rowCount + ');">Delete</a></p>';
    jQuery('#addedRows').append(recRow);
}

function removeRow(removeNum) {
    jQuery('#rowCount' + removeNum).remove();
}