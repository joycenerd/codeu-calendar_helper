function fetchTags() {
    const url = "/dashboard/tags";
    fetch(url)
        .then((response) => {
            return response.json();
        })
        .then((messages) => {
            const tagContainer = document.getElementById('draggable-2');
            if (messages.length == 0) {
                tagContainer.innerHTML = '<p>There are no tags yet.</p>';
            } else {
                tagContainer.innerHTML = '';
            }
            messages.forEach((message) => {
                const tagDiv = makeList(message);
                console.log(tagDiv);
                tagContainer.appendChild(tagDiv);
            });
        });
}
function myFunction() {
    var input, filter, ul, li, a, i, txtValue;
    input = document.getElementById("myInput");
    filter = input.value.toUpperCase();
    ul = document.getElementById("myUL");
    li = ul.getElementsByTagName("li");
    for (i = 0; i < li.length; i++) {
        a = li[i].getElementsByTagName("a")[0];
        txtValue = a.textContent || a.innerText;
        if (txtValue.toUpperCase().indexOf(filter) > -1) {
            li[i].style.display = "";
        } else {
            li[i].style.display = "none";
        }
    }
}

function makeList(message) {
    // Establish the array which acts as a data source for the list

    const listData = message.text.match(/#\w+/g);
    // Make a container element for the list
    const listContainer = document.createElement("div");
    if (listData == null) return listContainer; //!!!!!!!!!!!!!!!!!!!!!!! important

    // Set up a loop that goes through the items in listItems one at a time
    const numberOfListItems = listData.length;

    for (var i = 0; i < numberOfListItems; ++i) {
        // create an item for each one
        const listItem = document.createElement("li");
        const a = document.createElement("a");
        a.textContent = listData[i];
        a.setAttribute("href", "https://calendar.google.com/calendar/");
        listItem.appendChild(a);
        // Add listItem to the listElement
        listContainer.appendChild(listItem);

    }
    return listContainer;
}

function buildUI() {
    fetchTags();
}