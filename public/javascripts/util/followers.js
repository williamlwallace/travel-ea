let followersRequestOrder;

/**
 * Sets the follow button on destination and profile page depending if you follow this user/destination or not
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
        url = destinationRouter.controllers.backend.DestinationController.getDestination(
            id).url;
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
                    let following = countFormatter(
                        followingUsers + followingDests);

                    // Set following count to be sum of followed users and destinations
                    $('#following-count').html(following);
                } else if (type === "destination") {
                    followers = data.followerCount;
                }
                followers = countFormatter(followers);
                $('#followers-count').html(followers);
            }
        })
    })
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

/**
 * Handles scroll event for the profile followers/following modal
 * @param {Boolean} onFollowingTab Whether or not the user is on the following tab
 * @param {Number} id The id of the profile the user is on
 * @param {String} searchQuery The query to search by
 */
function profileScrollHandler(onFollowingTab, id, searchQuery) {
    if (($(this).innerHeight() + $(this).scrollTop())
        >= $(this)[0].scrollHeight - 100) {
        if (onFollowingTab) {
            if ($('#users-following-btn').hasClass('btn-primary')) {
                populateFollowingUsers(id, searchQuery, false).then(() => {
                    this.data("page-number", this.data("page-number") + 1);
                });
            } else {
                populateFollowingDestinations(id, searchQuery, false);
            }
        } else {
            populateFollowers(id, searchQuery, false);
        }
    }
}

/**
 * Handles scroll event for the destination followers modal
 * @param {Number} id The id of the profile the user is on
 * @param {String} searchQuery The query to search by
 */
function destinationPageScrollHandler(id, searchQuery) {
    if (($(this).innerHeight() + $(this).scrollTop())
        >= $(this)[0].scrollHeight - 100) {
        populateDestinationFollowers(id, searchQuery, false);
    }
}

/**
 * Click listener to display and populate and display followers modal on profile page.
 * In particular what the user is following.
 */
$("#following-summary").on('click', function () {
    $("#following-modal").modal("show");
    $("#following-tab").click();
});

/**
 * Click listener to display and populate and display followers modal on profile page.
 * In particular who is following a user.
 */
$('#follower-summary').on('click', function () {
    $("#following-modal").modal("show");
    $("#follower-tab").click();
});

/**
 * On click listener for the following tab in the modal
 */
$("#following-tab").on("click", function () {
    $('#followingSearch').val("");
    $('#dests-following-btn').attr('class', 'btn btn-outline-primary');
    $('#users-following-btn').attr('class', 'btn btn-primary');
    const id = parseInt(window.location.href.split("/").pop());
    followersRequestOrder = 1;
    populateFollowingUsers(id, "", true);
    const followersCardList = $("#followersCardList");
    followersCardList.scroll(
        profileScrollHandler.bind(followersCardList, true, id, ""));
});

/**
 * On click listener for the follower tab in the modal
 */
$("#follower-tab").on("click", function () {
    $('#followersSearch').val("");
    const id = parseInt(window.location.href.split("/").pop());
    followersRequestOrder = 1;
    populateFollowers(id, "", true);
    const followedByCardList = $("#followedByCardList");
    followedByCardList.scroll(
        profileScrollHandler.bind(followedByCardList, false, id, ""));
});

/**
 * On click listener to display and populate and display followers modal on destination page
 */
$('#destination-follower-summary').on('click', function () {
    $('#followersSearch').val("");
    $("#followers-modal").modal("show");
    const id = parseInt(window.location.href.split("/").pop());
    followersRequestOrder = 1;
    populateDestinationFollowers(id, "", true);
    const followedByCardList = $("#followedByCardList");
    followedByCardList.scroll(
        profileScrollHandler.bind(followedByCardList, false, id, ""));
});

/**
 * On click handler to change the selected buttons to destination on the following tabs
 */
$('#dests-following-btn').on('click', function () {
    $('#dests-following-btn').attr('class', 'btn btn-primary');
    $('#users-following-btn').attr('class', 'btn btn-outline-primary');
    $('#followingSearch').val("");
    const id = parseInt(window.location.href.split("/").pop());

    followersRequestOrder = 1;
    populateFollowingDestinations(id, "", true);
    const followersCardList = $("#followersCardList");
    followersCardList.scroll(
        profileScrollHandler.bind(followersCardList, true, id, ""));
});

/**
 * On click handler to change the selected buttons to users on the following tabs
 */
$('#users-following-btn').on('click', function () {
    $('#dests-following-btn').attr('class', 'btn btn-outline-primary');
    $('#users-following-btn').attr('class', 'btn btn-primary');
    $('#followingSearch').val("");
    const id = parseInt(window.location.href.split("/").pop());
    followersRequestOrder = 1;
    populateFollowingUsers(id, "", true);
});

/**
 * Handler to handle the user typing in the search box
 */
$('#followingSearch').on('keyup', function (e) {
    searchFollowing($(this).val());
});

/**
 * Handler to handle the user typing in the search box
 */
$('#followersSearch').on('keyup', function (e) {
    searchFollowers($(this).val());
});

/**
 * Calls the function to populate the followers section using searched text
 * @param textInput The text to search by
 */
function searchFollowers(textInput) {
    const url = window.location.href.split("/");
    const id = parseInt(url.pop());
    const onDestination = url.pop() === "destinations";
    const followedByCardList = $("#followedByCardList");
    followersRequestOrder = 1;

    if (onDestination) {
        populateDestinationFollowers(id, textInput, true);
        followedByCardList.scroll(
            destinationPageScrollHandler.bind(followedByCardList, id,
                textInput));
    } else {
        populateFollowers(id, textInput, true);
        followedByCardList.scroll(
            profileScrollHandler.bind(followedByCardList, false, id,
                textInput));
    }
}

/**
 * Calls the function to populate the following section using searched text
 * @param textInput The text to search by
 */
function searchFollowing(textInput) {
    const id = parseInt(window.location.href.split("/").pop());
    followersRequestOrder = 1;
    const followersCardList = $("#followersCardList");
    if ($('#users-following-btn').hasClass('btn-primary')) {
        populateFollowingUsers(id, textInput, true);
        followersCardList.scroll(
            profileScrollHandler.bind(followersCardList, true, id, textInput));
    } else {
        populateFollowingDestinations(id, textInput, true);
        followersCardList.scroll(
            profileScrollHandler.bind(followersCardList, true, id, textInput));
    }
}

/**
 * Function to populate the followers modal with users that the given user is following
 * @param {Number} userId Id of user who's profile is being viewed
 * @param {String} searchQuery The name to search by
 * @param {Boolean} clearFollowers Whether or not to clear the followers already displayed
 */
function populateFollowingUsers(userId, searchQuery, clearFollowers) {
    const url = new URL(
        profileRouter.controllers.backend.ProfileController.getPaginatedFollowingUsers(
            userId).url, window.location.origin);

    url.searchParams.append("requestOrder", followersRequestOrder);
    url.searchParams.append("pageNum", followersRequestOrder);

    if (searchQuery) {
        url.searchParams.append("searchQuery", searchQuery);
    }

    return get(url).then(response => {
        return response.json()
        .then(followers => {
            if (response.status !== 200) {
                showErrors(followers);
            } else {
                if (followersRequestOrder === followers.requestOrder) {
                    followersRequestOrder = followersRequestOrder + 1;
                    createUserFollowerCard(followers.data, clearFollowers);
                }
            }
        });
    });
}

/**
 * Function to populate the followers modal with destinations that the given user is following
 * @param {Number} userId Id of user who's profile is being viewed
 * @param {String} searchQuery The name to search by
 * @param {Boolean} clearFollowers Whether or not to clear the followers already displayed
 */
function populateFollowingDestinations(userId, searchQuery, clearFollowers) {
    const url = new URL(
        destinationRouter.controllers.backend.DestinationController.getDestinationsFollowedByUser(
            userId).url, window.location.origin);

    url.searchParams.append("requestOrder", followersRequestOrder);
    url.searchParams.append("pageNum", followersRequestOrder);

    if (searchQuery) {
        url.searchParams.append("searchQuery", searchQuery);
    }

    return get(url).then(response => {
        return response.json()
        .then(followers => {
            if (response.status !== 200) {
                showErrors(followers);
            } else {
                if (followersRequestOrder === followers.requestOrder) {
                    followersRequestOrder = followersRequestOrder + 1;
                    createDestinationFollowerCard(followers.data,
                        clearFollowers);
                }
            }
        });
    });
}

/**
 * Function to populate the followers modal with users that the given user is followed by
 * @param {Number} userId Id of user who's profile is being viewed
 * @param {String} searchQuery The name to search by
 * @param {Boolean} clearFollowers Whether or not to clear the followers already displayed
 */
function populateFollowers(userId, searchQuery, clearFollowers) {
    const url = new URL(
        profileRouter.controllers.backend.ProfileController.getPaginatedFollowerUsers(
            userId).url, window.location.origin);

    url.searchParams.append("requestOrder", followersRequestOrder);
    url.searchParams.append("pageNum", followersRequestOrder);

    if (searchQuery) {
        url.searchParams.append("searchQuery", searchQuery);
    }

    return get(url).then(response => {
        return response.json()
        .then(followers => {
            if (response.status !== 200) {
                showErrors(followers);
            } else {
                if (followersRequestOrder === followers.requestOrder) {
                    followersRequestOrder = followersRequestOrder + 1;
                    createUserFollowedByCard(followers.data, clearFollowers);
                }
            }
        });
    });
}

/**
 * Populates the followers modal on the destination page with all users following the given destination
 * @param {Number} destinationId - ID of destination displayed on page
 * @param {String} searchQuery The name to search by
 * @param {Boolean} clearFollowers Whether or not to clear the followers already displayed
 */
function populateDestinationFollowers(destinationId, searchQuery,
    clearFollowers) {
    const url = new URL(
        destinationRouter.controllers.backend.DestinationController.getDestinationFollowers(
            destinationId).url, window.location.origin);

    url.searchParams.append("requestOrder", followersRequestOrder);
    url.searchParams.append("pageNum", followersRequestOrder);

    if (searchQuery) {
        url.searchParams.append("searchQuery", searchQuery);
    }

    return get(url).then(response => {
        return response.json()
        .then(followers => {
            if (response.status !== 200) {
                showErrors(followers);
            } else {
                if (followersRequestOrder === followers.requestOrder) {
                    followersRequestOrder = followersRequestOrder + 1;
                    createUserFollowedByCard(followers.data, clearFollowers);
                }
            }
        });
    });
}

/**
 * Create follower cards for users that a user is following
 * @param {Array} users - List of all users that need to be made into cards
 * @param {Boolean} clearFollowers Whether or not to clear the followers already displayed
 */
function createUserFollowerCard(users, clearFollowers) {
    if (clearFollowers) {
        $("#followersCardList").html("");
    }

    if (users.length < 1 && clearFollowers) {
        $("#followersCardList").html(
            '<label id="no-following">No users found</label>');
    } else {
        users.forEach((user) => {
            const template = $("#followerCardTemplate").get(0);
            const clone = template.content.cloneNode(true);

            $(clone).find("#follower-summary-name").append(
                user.firstName + ' ' + user.lastName);
            if (user.profilePhoto) {
                $(clone).find("#follower-picture").attr("src",
                    "../../user_content/"
                    + user.profilePhoto.thumbnailFilename);
            }
            $(clone).find("#follower-card").attr("data-id",
                user.userId.toString());
            // Set follower count on user card
            const followerCount = countFormatter(user.followerUsersCount);
            $(clone).find("#follower-summary-follower-count").append(
                followerCount + " Followers");
            $(clone).find("#follower-card").click(function() {location.href = `/profile/${user.userId}`});

            $("#followersCardList").get(0).appendChild(clone);
        });
    }
}

/**
 * Create follower cards for users that are following a user
 * @param {Array} users - List of all users that need to be made into cards
 * @param {Boolean} clearFollowing Whether or not to clear the followed already displayed
 */
function createUserFollowedByCard(users, clearFollowing) {
    if (clearFollowing) {
        $("#followedByCardList").html("");
    }

    if (users.length < 1 && clearFollowing) {
        $("#followedByCardList").html(
            '<label id="no-followers">No followers found</label>');
    } else {
        users.forEach((user) => {
            const template = $("#followerCardTemplate").get(0);
            const clone = template.content.cloneNode(true);

            $(clone).find("#follower-summary-name").append(
                user.firstName + ' ' + user.lastName);
            if (user.profilePhoto) {
                $(clone).find("#follower-picture").attr("src",
                    "../../user_content/"
                    + user.profilePhoto.thumbnailFilename);
            }
            $(clone).find("#follower-card").attr("data-id",
                user.userId.toString());

            // Set follower count on user card
            const followerCount = countFormatter(user.followerUsersCount);
            $(clone).find("#follower-summary-follower-count").append(
                followerCount + " Followers");
            $(clone).find("#follower-card").click(function() {location.href = `/profile/${user.userId}`});
            $("#followedByCardList").get(0).appendChild(clone);

        });
    }
}

/**
 * Create follower cards for destinations that a user is following
 * @param {Array} destinations - List of all destinations that need to be made into cards
 * @param {Boolean} clearFollowers Whether or not to clear the followers already displayed
 */
function createDestinationFollowerCard(destinations, clearFollowers) {
    if (clearFollowers) {
        $("#followersCardList").html("");
    }

    if (destinations.length < 1 && clearFollowers) {
        $("#followersCardList").html(
            '<label id="no-following">No destinations found</label>');
    } else {
        destinations.forEach((dest) => {
            const template = $("#followerCardTemplate").get(0);
            const clone = template.content.cloneNode(true);

            $(clone).find("#follower-summary-name").append(dest.name);
            $(clone).find("#follower-picture").attr("src",
                "/assets/images/default-destination-primary.png");
            if (dest.primaryPhoto) {
                $(clone).find("#follower-picture").attr("src",
                    "../../user_content/"
                    + dest.primaryPhoto.thumbnailFilename);
            }
            $(clone).find("#follower-card").attr("data-id", dest.id.toString());

            // Set follower count on destination card
            const followerCount = countFormatter(dest.followerCount);
            $(clone).find("#follower-summary-follower-count").append(
                followerCount + " Followers");
            $(clone).find("#follower-card").click(function() {location.href = `/destinations/${dest.id}`});
            $("#followersCardList").get(0).appendChild(clone);
        });
    }
}

/**
 * Create follower cards for destinations that a user is following for the explore page
 * @param {Array} destinations - List of all destinations that need to be made into cards
 */
function createDestinationFollowerCardExplorePage(destinations) {
    $("#followersCardListDestinations").html("");

    if (destinations.length < 1) {
        $("#followersCardListDestinations").html(
            '<label id="no-trending">No Destinations are Currently Trending</label>');
    } else {
        destinations.forEach((dest) => {
            const template = $("#followerCardTemplate").get(0);
            const clone = template.content.cloneNode(true);

            $(clone).find("#follower-summary-name").append(dest.name);
            $(clone).find("#follower-picture").attr("src",
                "/assets/images/default-destination-primary.png");
            if (dest.primaryPhoto) {
                $(clone).find("#follower-picture").attr("src",
                    "../../user_content/"
                    + dest.primaryPhoto.thumbnailFilename);
            }
            // Set follower count on destination card
            const followerCount = countFormatter(dest.followerCount);
            $(clone).find("#follower-summary-follower-count").append(
                followerCount + " Followers");
            $(clone).find("#follower-card").click(function() {location.href = `/destinations/${dest.id}`});
            $("#followersCardListDestinations").get(0).appendChild(clone);

        });
    }
}
