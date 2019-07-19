function postTags() {
    const url = '/dashboard/tagManage';
    fetch(url)
        .then((response) => {
            return response.json();
        })
        .then((tags) => {

            var method = "update";

        });
}
function fetchTags() {
    const url = '/dashboard/tags';
    fetch(url)
        .then((response) => {
            return response.json();
        })
        .then((tags) => {
            const tagContainer = document.getElementById('card-body-withtags');
            console.log(tags.length);
            if (tags.length == 0) {
                tagContainer.innerHTML = '<p>There are no tags yet.</p>';
            } else {
                tagContainer.innerHTML = 'hhh';
            }
            tags.forEach((tag) => {
                const tagDiv = buildtagDiv(tag);
                tagContainer.appendChild(tagDiv);
            });
        });
}

function buildtagDiv(tag) {
    const usernameDiv = document.createElement('div');
    usernameDiv.classList.add('col', 'text-left', 'p-2');
    usernameDiv.appendChild(document.createTextNode(tag.userId));

    const timeDiv = document.createElement('small');
    timeDiv.classList.add('col', 'text-right', 'p-2');
    timeDiv.appendChild(document.createTextNode(new Date(tag.eventDateTime)));

    const headerDiv = document.createElement('div');
    headerDiv.classList.add('row', 'border-bottom');
    headerDiv.appendChild(usernameDiv);
    headerDiv.appendChild(timeDiv);

    const textDiv = document.createElement('div');
    textDiv.classList.add('col', 'text-justify', 'p-2');
    textDiv.appendChild(document.createTextNode(tag.tag));

    const bodyDiv = document.createElement('div');
    bodyDiv.classList.add('row');
    bodyDiv.appendChild(textDiv);

    const tagInnerDiv = document.createElement('div');
    tagInnerDiv.classList.add('col', 'container');
    tagInnerDiv.appendChild(headerDiv);
    tagInnerDiv.appendChild(bodyDiv);

    const tagDiv = document.createElement('div');
    tagDiv.classList.add(
        'row', 'border-0', 'rounded', 'shadow', 'mb-3', 'bg-light');
    tageDiv.appendChild(tagInnerDiv);

    return tagDiv;
}

// Fetch data and populate the UI of the page.
function buildUIshowtags() {
    fetchTags();
}