class DestinationMap {
    /**
     * Initialises google maps on destination page and dynamically adds destination markers
     */
    constructor(options, creativeMode, userId) {
        // New map
        this.map = new google.maps.Map(document.getElementById('map'), options);
        this.markers = [];
        this.newMarker;
        this.markerCluster;
        this.userId = userId;
        this.creativeMode = creativeMode;
        //Setting public and private marker images, resized
        this.markerPublic = {
            url: 'https://image.flaticon.com/icons/svg/149/149060.svg',
            scaledSize: new google.maps.Size(30, 30)
        };
        this.markerPrivate = {
            url: 'https://image.flaticon.com/icons/svg/139/139012.svg',
            scaledSize: new google.maps.Size(30, 30)
        };
    }

    /**
     * Like places a marker on the map and like its like gnarly.
     * @param location
     * @param icon
     * @returns {google.maps.Marker}
     */
    placeMarker(location, icon) {
        const marker = new google.maps.Marker({
            position: location,
            map: this.map
        });

        marker.setIcon(icon);
        this.markers.push(marker);
        return marker
    }

    /**
     * Resets and removes the newMarker
     */
    removeNewMarker() {
        this.newMarker.setPosition(null);
        this.newMarker = undefined;
    }

    /**
     *  Pans map to the new marker
     */
    panToNewMarker() {
        const latlng = this.newMarker.getPosition();
        this.map.panTo(latlng);
    }

    /**
     * Set New marker position
     */
    setNewMarker(lat, lng, icon) {
        if (lat && !lng) {
            lng = this.newMarker ? this.newMarker.getPosition().lng() : 0
        } else if (!lat && lng) {
            lat = this.newMarker ? this.newMarker.getPosition().lat() : 0
        }
        const latlng = new google.maps.LatLng(parseFloat(lat), parseFloat(lng));
        if (this.newMarker) {
            this.newMarker.setPosition(latlng);
        } else {
            this.newMarker = this.placeMarker(latlng, icon);
        }
    }

    /**
     * Inserts marker on map
     * @param {JSON} props contain destination coords, destination information, and styling
     */
    addDestination(props) {
        const marker = this.placeMarker(props.coords, props.iconImage);
        // Check content
        if (props.content) {
            let infoWindow = new google.maps.InfoWindow({
                content: props.content
            });
            // if content exists then make a info window
            marker.addListener('click', function () {
                if (activeInfoWindow) {
                    activeInfoWindow.close();
                }
                infoWindow.open(map, marker);
                activeInfoWindow = infoWindow;
            });
        }
    }

    /**
     * Goes through all destinations and adds to map
     * @param {Array} destinations list of destinations
     */
    addDestinations(destinations) {
        // Loop through markers list and add them to the map
        for (const destination of destinations) {
            this.addDestination(destination);
        }
        this.clusterMarkers();
    }

    /**
     * Goes through all destinations and removes from map
     */
    removeDestinations() {
        // Loop through markers list and add them to the map
        for (const marker of this.markers) {
            marker.setMap(null);
        }
        this.markers = [];
    }

    /**
     * Clusters all of the destination markers on the map.
     */
    clusterMarkers() {
        const markerCluster = new MarkerClusterer(this.map, this.markers,
            {imagePath: '/assets/images/markerClusterer/m'});
        // add listner to stop marker change on cluster click
        google.maps.event.addListener(this.map, 'zoom_changed', function() {
            map.creativeMode = false;
            setTimeout(() => {
                map.creativeMode = true;
            }, 10);
        });
    }

    /**
     * Populates the markers list with props which can be iterated over to dynamically add destination markers
     */
    populateMarkers() {
        const destinations = [];
        this.removeDestinations();
        return get(
            destinationRouter.controllers.backend.DestinationController.getAllDestinations(
                this.userId).url)
        .then(response => {
            return response.json()
            .then(json => {
                if (response.status !== 200) {
                    document.getElementById("otherError").innerHTML = json;
                } else {
                    for (const dest in json) {
                        //Link to detailed destination in info window
                        const destination = destinationRouter.controllers.frontend.DestinationController.detailedDestinationIndex(
                            json[dest].id).url;

                        //Setting public and privacy icon in info window
                        let privacySrc;
                        json[dest].isPublic
                            ? privacySrc = "/assets/images/public.png"
                            : privacySrc = "/assets/images/private.png";

                        destinations.push({
                            coords: {
                                lat: json[dest].latitude,
                                lng: json[dest].longitude
                            },
                            iconImage: json[dest].isPublic ? this.markerPublic
                                : this.markerPrivate,
                            content: '<a class="marker-link" title="View detailed destination" href="'
                                + destination + '"><h3 style="display:inline">'
                                + json[dest].name
                                + '</h3></a>&nbsp;&nbsp;&nbsp;<img src="'
                                + privacySrc
                                + '"height="20" style="margin-bottom:13px">'
                                + '<p><b>Type:</b> ' + json[dest].destType
                                + '<br>'
                                + '<b>District:</b> ' + json[dest].district
                                + '<br>'
                                + '<b>Latitude:</b> ' + json[dest].latitude.toFixed(2)
                                + '<br>'
                                + '<b>Longitude:</b> ' + json[dest].longitude.toFixed(2)
                                + '<br>'
                                + '<b>Country:</b> ' + json[dest].country.name
                                + '</p>'
                        });
                    }
                    this.addDestinations(destinations);
                }
            });
        });
    }
}