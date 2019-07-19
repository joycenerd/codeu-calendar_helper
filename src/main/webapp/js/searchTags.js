function getTags() {
    const url = '/dashboard/tags';
    fetch(url)
        .then((response) => {
            return response.json();
        })
        .then((tagData) => {
            const tagContainer = document.getElementById('myUL');
            console.log(tagData.length);
            if (tagData.length == 0) {
                tagContainer.innerHTML = '<p>There are no tags yet.</p>';
            } else {
                tagContainer.innerHTML = '';
            }
            tagData.forEach((tag) => {
                const tagDiv = makeList(tag);
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

function makeList(tag) {

    const listData = tag.tag;
    // Make a container element for the list
    const listContainer = document.createElement("div");
    if (listData == null) return listContainer; //!!!!!!!!!!!!!!!!!!!!!!! important
    const listItem = document.createElement("li");
    const a = document.createElement("a");
    a.textContent = listData;
    a.setAttribute("href", "https://calendar.google.com/calendar/");
    listItem.appendChild(a);
    // Add listItem to the listElement
    listContainer.appendChild(listItem);

    return listContainer;
}

