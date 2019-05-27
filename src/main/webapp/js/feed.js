// Fetch messages and add them to the page.
function fetchMessages(){
  const url = '/feed';
  fetch(url).then((response) => {
      return response.json();
      }).then((messages) => {
        const messageContainer = document.getElementById('message-container');
        if(messages.length == 0){
        messageContainer.innerHTML = '<p>There are no posts yet.</p>';
        }
        else{
        messageContainer.innerHTML = '';  
        }
        messages.forEach((message) => {  
            const messageDiv = buildMessageDiv(message);
            messageContainer.appendChild(messageDiv);
            });
        });
}

function buildMessageDiv(message){
  const usernameDiv = document.createElement('div');
  usernameDiv.classList.add('col', 'text-left', 'p-2');
  usernameDiv.appendChild(document.createTextNode(message.user));

  const timeDiv = document.createElement('small');
  timeDiv.classList.add('col', 'text-right', 'p-2');
  timeDiv.appendChild(document.createTextNode(new Date(message.timestamp)));

  const headerDiv = document.createElement('div');
  headerDiv.classList.add('row', 'border-bottom');
  headerDiv.appendChild(usernameDiv);
  headerDiv.appendChild(timeDiv);

  const textDiv = document.createElement('div');
  textDiv.classList.add('col', 'text-justify', 'p-2');
  textDiv.appendChild(document.createTextNode(message.text));

  const bodyDiv = document.createElement('div');
  bodyDiv.classList.add('row');
  bodyDiv.appendChild(textDiv);

  const messageInnerDiv = document.createElement('div');
  messageInnerDiv.classList.add('col', 'container');
  messageInnerDiv.appendChild(headerDiv);
  messageInnerDiv.appendChild(bodyDiv);

  const messageDiv = document.createElement('div');
  messageDiv.classList.add("row", "border-0", "rounded", "shadow", "mb-3", "bg-light");
  messageDiv.appendChild(messageInnerDiv);

  return messageDiv;
}

// Fetch data and populate the UI of the page.
function buildUI(){
  fetchMessages();
}
