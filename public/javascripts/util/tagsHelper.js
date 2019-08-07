$('#tags').on('keydown', function(e){
    //Need to keep depreciated symbols for older browsers
    const key = e.which || e.keyCode || e.key;
    const tag = $('#tags input').val();
    if(key === 13 && tag !== "") {
        const tagEl = $("<li></li>").text(tag);
        const spacer = $("<li></li>").text("fillerfilllerfillerfillerfil").attr('class', 'spacer');
        const closeBtn = $("<button></button>").attr('class', 'close-icon').click(deleteTag);
        tagEl.append(closeBtn);
        //Remove spacer, append new tag, append spacer
        $('li').remove('.spacer');
        $('#tags ul').append(tagEl);
        $('#tags ul').append(spacer);
        //clear input
        $('#tags input').val("");
        $('#tags ul').scrollLeft(500);

    } else if (key === 8 && tag === "") {
        const spacer = $("<li></li>").text("fillerfilllerfillerfillerfil").attr('class', 'spacer');
        //Remove spacer, remove last tag, append spacer
        $('li').remove('.spacer');
        $('#tags ul li:last-child').remove();
        $('#tags ul').append(spacer);
    }
});

function deleteTag() {
    $(this).parent().remove();
};

function getTags() {
    const tags = [];
    for (const li of $('#tags ul li')) {
        tags.push(li.innerText);
    }
    return tags.slice(0, -1);
}