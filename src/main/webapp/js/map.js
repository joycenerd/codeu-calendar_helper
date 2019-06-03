var map, labelIndex = 1;

function createMap(){
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 37.422, lng: -122.084},
    zoom: 16
  });

  addMarker();
  console.log( toString(labelIndex) );
}

function addMarker(){
  const trexMarker = new google.maps.Marker({
    position: {lat: 37.421903, lng: -122.084674},
    map: map,
    label: labelIndex.toString(),
    title: 'Stan the T-Rex'
  });
  ++labelIndex;

  var trexInfoWindow = new google.maps.InfoWindow({
    content: 'This is Stan, the T-Rex statue.'
  });
  //trexInfoWindow.open(map, trexMarker);

  trexMarker.addListener('click', function() {
    trexInfoWindow.open(map, trexMarker);
  });

}
