$(document).ready(function () {
    getUserId().then(userId => {
        mainFeed = new NewsFeed(userId, 'main-feed',
            newsFeedRouter.controllers.backend.NewsFeedController.getMainNewsFeed().url);
    });
});