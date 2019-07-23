function fetchTags() {
    const url = '/dashboard/tags';
    fetch(url)
        .then((response) => {
            return response.json();
        })
        .then((tagData) => {
            const tagContainer = document.getElementById('card-body-withtags');
            //console.log(tagData.length);
            if (tagData.length == 0) {
                tagContainer.innerHTML = '<p>There are no tags yet.</p>';
            } else {
                tagContainer.innerHTML = '';

            }
            tagData.forEach((tag) => {
                const tagDiv = buildtagDiv(tag);
                tagContainer.appendChild(tagDiv);
            });
        });
}

function buildtagDiv(tag) {
    const timeDiv = document.createElement('small');
    timeDiv.classList.add('col', 'text-right', 'p-2');
    timeDiv.appendChild(document.createTextNode(new Date(tag.eventDateTime)));

    const headerDiv = document.createElement('div');
    headerDiv.classList.add('row', 'border-bottom');
    headerDiv.appendChild(timeDiv);

    const textDiv = document.createElement('div');
    textDiv.classList.add('col', 'text-justify', 'mystyle');
    textDiv.appendChild(document.createTextNode(tag.tag));

    const bodyDiv = document.createElement('div');
    bodyDiv.classList.add('row', 'change');
    bodyDiv.setAttribute('id', "hover");
    bodyDiv.appendChild(textDiv);

    const tagInnerDiv = document.createElement('div');
    tagInnerDiv.classList.add('col', 'container');
    tagInnerDiv.appendChild(headerDiv);
    tagInnerDiv.appendChild(bodyDiv);

    const tagDiv = document.createElement('div');
    tagDiv.classList.add('row', 'border-0', 'rounded', 'shadow', 'mb-3', 'bg-light');
    tagDiv.appendChild(tagInnerDiv);

    return tagDiv;
}
