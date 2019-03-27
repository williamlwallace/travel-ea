# Design Decisions

#### Json serving API
Creating an API that serves data to be accessed anywhere allows for a more advanced website by letting us grab data from outside the servers network. It also leaves us with a degree of seperation between front/backend, which helps us throught the development proccess to keep our code tidy.
We decided to use json for our API requests because of its lightweight, easy readibility, great support throught our development languages and frameworks.

#### JS fetching
Play renders the pages on the server and sends this to the clients, Js allows us to change this without getting play to rerender and a browser refresh. Js fetching allows us to get, update, remove and validate data without needing to rerender and refresh. For these resons we have decided to use js to fetch data (except for the initial data that can be directly rendered with Twirl).

#### Controller packages
We decided to sperate our controllers into two packages, controllers.frontend and controllers.backend. Because there is a large degree of seperation between our front and back end, they shouldnt directly interact with eachother. Seperating them into diffrent files/controllers helps ensure this.

#### Bridging models

#### Auth







