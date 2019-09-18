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

$("#following-summary").on('click', function() {
    $("#following-modal").modal("show");
    $("#following-tab").click();
    const id = window.location.href.split("/").pop();
    populateFollowingUsers(id);
    populateFollowers(id);
});

$('#follower-summary').on('click', function() {
    $("#following-modal").modal("show");
    $("#follower-tab").click();
    const id = window.location.href.split("/").pop();
    populateFollowers(id);
    populateFollowingUsers(id);
});

/**
 * On click handler to change the selected buttons to destination on the following tabs
 */
$('#dests-following-btn').on('click', function() {
    $('#dests-following-btn').attr('class', 'btn btn-primary');
    $('#users-following-btn').attr('class', 'btn btn-outline-primary');
    const id = window.location.href.split("/").pop();
    populateFollowingDestinations(id);
});

/**
 * On click handler to change the selected buttons to users on the following tabs
 */
$('#users-following-btn').on('click', function() {
    $('#dests-following-btn').attr('class', 'btn btn-outline-primary');
    $('#users-following-btn').attr('class', 'btn btn-primary');
    const id = window.location.href.split("/").pop();
    populateFollowingUsers(id);
});

/**
 * Function to populate the followers modal with users that the given user is following
 * @param userId {Number} Id of user whos profile is being viewed
 */
function populateFollowingUsers(userId) {
    get(profileRouter.controllers.backend.ProfileController.getPaginatedFollowingUsers(
        userId).url)
    .then(response => {
        response.json()
        .then(followers => {
            if (response.status !== 200) {
                showErrors(followers);
            } else {
                createUserFollowerCard(followers.data);
            }
        });
    });
}

/**
 * Function to populate the followers modal with destinations that the given user is following
 * @param userId {Number} Id of user whos profile is being viewed
 */
function populateFollowingDestinations(userId) {
    get(destinationRouter.controllers.backend.DestinationController.getDestinationsFollowedByUser(
        userId).url)
    .then(response => {
        response.json()
        .then(followers => {
            if (response.status !== 200) {
                showErrors(followers);
            } else {
                createDestinationFollowerCard(followers.data);
            }
        });
    });
}

/**
 * Function to populate the followers modal with users that the given user is followed by
 * @param userId {Number} Id of user whos profile is being viewed
 */
function populateFollowers(userId) {
    get(profileRouter.controllers.backend.ProfileController.getPaginatedFollowerUsers(
        userId).url)
    .then(response => {
        response.json()
        .then(followers => {
            if (response.status !== 200) {
                showErrors(followers);
            } else {
                createUserFollowedByCard(followers.data);
            }
        });
    });
}

/**
 * Create follower cards for users that a user is following
 * @param users {List} List of all users that need to be made into cards
 */
function createUserFollowerCard(users) {
    $("#followersCardList").html("");
    users.forEach((user) => {
        const template = $("#followerCardTemplate").get(0);
        const clone = template.content.cloneNode(true);

        $(clone).find("#name").append(user.firstName + ' ' + user.lastName);
        if (user.profilePhoto) {
            $(clone).find("#follower-picture").attr("src", "../../user_content/" + user.profilePhoto.thumbnailFilename);
        }
        $("#followersCardList").get(0).appendChild(clone);
    });
}

/**
 * Create follower cards for users that are following a user
 * @param users {List} List of all users that need to be made into cards
 */
function createUserFollowedByCard(users) {
    $("#followedByCardList").html("");
    users.forEach((user) => {
        const template = $("#followerCardTemplate").get(0);
        const clone = template.content.cloneNode(true);

        $(clone).find("#name").append(user.firstName + ' ' + user.lastName);
        if (user.profilePhoto) {
            $(clone).find("#follower-picture").attr("src", "../../user_content/" + user.profilePhoto.thumbnailFilename);
        }
        $("#followedByCardList").get(0).appendChild(clone);
    });
}

/**
 * Create follower cards for destinations that a user is following
 * @param destinations {List} List of all destinations that need to be made into cards
 */
function createDestinationFollowerCard(destinations) {
    $("#followersCardList").html("");
    destinations.forEach((dest) => {
        const template = $("#followerCardTemplate").get(0);
        const clone = template.content.cloneNode(true);

        $(clone).find("#name").append(dest.name);
        if (dest.primaryPhoto) {
            $(clone).find("#follower-picture").attr("src", "../../user_content/" + dest.primaryPhoto.thumbnailFilename);
        }
        $("#followersCardList").get(0).appendChild(clone);
    });
}