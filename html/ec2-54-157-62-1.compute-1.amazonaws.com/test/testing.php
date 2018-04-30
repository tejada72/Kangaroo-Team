<!DOCTYPE html>
<html>
  <head>
    <title>Simple Map</title>
    <meta name="viewport" content="initial-scale=1.0">
    <meta charset="utf-8">
    <style>
      /* Always set the map height explicitly to define the size of the div
       * element that contains the map. */
      #map {
        height: 100%;
      }
      /* Optional: Makes the sample page fill the window. */
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
      }
    </style>
  </head>
  <body>
    <div id="map"></div>
    <script>
      function initMap() {
        // Create a map object and specify the DOM element for display.
        var map = new google.maps.Map(document.getElementById('map'), {
          center: {lat: 39.711493, lng: -75.117101},
          zoom: 15
        });
      }

    </script>
	<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAkwQ2Z4nVsENwFjdr1BaCeq2QuYLCk5wI&callback=initMap"
    async defer></script>
  </body>
</html>
