/**
 * Runs when the page is loaded. Initialises the paginationHelper objects and loads feed
 */
$(document).ready(function () {
   populateTrendingUsers();
   populateTrendingDestinations()
});

/**
 * Adds the current trending users
 */
function populateTrendingUsers() {
   const url = newsFeedRouter.controllers.backend.NewsFeedController.getTrendingUsers().url;
   get(url)
   .then(response => {
      response.json().then(users => {
         if (response.status !== 200) {
            showErrors(users);
         } else {
            createUserFollowerCard(users, false);
         }
      });
   });
}

/**
 * Adds the current trending destinations
 */
function populateTrendingDestinations() {
   const url = newsFeedRouter.controllers.backend.NewsFeedController.getTrendingDestinations().url;
   get(url)
   .then(response => {
      response.json().then(dests => {
         if (response.status !== 200) {
            showErrors(dests);
         } else {
            createDestinationFollowerCardExplorePage(dests);
         }
      });
   });
}