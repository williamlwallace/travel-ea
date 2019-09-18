/**
 * Sets the follow button depending if you follow this user/destination or not
 *
 * @param type is type of thing to follow/unfollow eg. "profile", "destination"
 */
function loadFollowBtn(type) {
    let followBtn =
        "<button id=\"follow-btn\" class=\"btn btn-primary\" onclick=\"followToggle(type_)\">\n"
        + "<i class=\"fas fa-user-friends\"></i>  Follow\n"
        + "</button>\n";

    let followingBtn =
        "<button id=\"following-btn\" class=\"btn btn-success\" onclick=\"followToggle(type_)\">Following  <i class=\"fas fa-check\"></i></button>";

    const id = window.location.href.split("/").pop();
    getUserId().then(userId => {
        let url = null;
        if (type === "profile") {
            if (userId === id) {
                return;
            }
            url = userRouter.controllers.backend.UserController.getFollowerStatus(
                id).url;
        } else if (type === "destination") {
            url = destinationRouter.controllers.backend.DestinationController.getFollowerStatus(
                id).url;
        }
        get(url).then(response => {
            response.json().then(following => {
                if (following) {
                    $('#follow-btn-parent').html(followingBtn);
                } else {
                    $('#follow-btn-parent').html(followBtn);
                }
            });
        });
    });
}

/**
 * Updates the follower count on front-end by getting the follower/following count from database
 *
 * @param id is profile id of profile to get count for
 * @param type is type of thing to get count from eg. "profile", "destination"
 */
function updateFollowerCount(id, type) {
    let url = null;
    if (type === "profile") {
        url = profileRouter.controllers.backend.ProfileController.getProfile(
            id).url;
    } else if (type === "destination") {
        url = destinationRouter.controllers.backend.DestinationController.getDestination(id).url;
    }
    get(url)
    .then(response => {
        response.json()
        .then(data => {
            if (response.status !== 200) {
                toast("Error", "Unable to retrieve followers/following count",
                    "danger", 5000);
            } else {
                // Set follower count here
                let followers = 0;
                if (type === "profile") {
                    let followingUsers = data.followingUsersCount;
                    let followingDests = data.followingDestinationsCount;
                    followers = data.followerUsersCount;
                    followingUsers = followerCountFormatter(followingUsers);
                    followingDests = followerCountFormatter(followingDests);

                    // Set following count to be sum of followed users and destinations
                    $('#following-count').html(followingUsers + followingDests);
                } else if (type === "destination") {
                    followers = data.followerCount;
                }
                followers = followerCountFormatter(followers);
                $('#followers-count').html(followers);
            }
        })
    })
}

/**
 * Shortens numbers by adding a 'k' on the end to represent thousands
 *
 * @param num is number to format
 * @returns {number} formatted number
 */
function followerCountFormatter(num) {
    return Math.abs(num) > 999 ? Math.sign(num) * ((Math.abs(num)
        / 1000).toFixed(1)) + 'k' : Math.sign(num) * Math.abs(num)
}

/**
 * Follow/unfollow a destination/user
 *
 * @param type is type of thing to follow/unfollow eg. "profile", "destination"
 */
function followToggle(type) {
    const id = window.location.href.split("/").pop();
    let URL = null;
    if (type === "profile") {
        URL = userRouter.controllers.backend.UserController.toggleFollowerStatus(
            id).url;
    } else if (type === "destination") {
        URL = destinationRouter.controllers.backend.DestinationController.toggleFollowerStatus(
            id).url;
    }
    const handler = (status, json) => {
        if (status !== 200) {
            toast("Error", "Unable to toggle follow", "danger",
                5000);
        } else {
            loadFollowBtn(type);
            updateFollowerCount(id, type);
        }
    };
    const reqData = new ReqData(requestTypes["TOGGLE"], URL,
        handler);
    undoRedo.sendAndAppend(reqData);
}