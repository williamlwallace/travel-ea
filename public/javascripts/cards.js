function populateCard(name, age, gender, nationalities, travellerTypes) {
    $("#card-header").html(header);
    $("#age").html("Age: " + age);
    $("#gender").html("Gender: " + gender);
    $("#nationalities").html("Nationalities: " + nationalities);
    $("#traveller-type").html("Traveller Types: " + travellerTypes);
}