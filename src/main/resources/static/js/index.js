const modalType = {
    confirmation: 0,
    info: 1,
    orderList: 2
}

var numberSongs;
var randomSongs;
var currentSong = -1;
var audio = document.getElementById("player");

$("#document").ready(function () {
    reloadArraySongs();
    setDataTableConfigAndEvents();
    setPlaylistEvent();
    setEvents();
})

function getTime(seconds) {
    var min = Math.trunc(seconds / 60);
    var sec = Math.round(seconds % 60);
    return min + ":" + (sec < 10 ? "0" + sec : sec);
}

function reloadArraySongs() {
    numberSongs = $("#tbodySong").find("tr[data-name]").toArray();
    currentSong = -1;
}

function updateListSong() {
    $.get("song/updateSongList", function (data) {
        if (!data.includes("<html>")) {
            $("#divListSong").html(data);
            $("#listPlaylist").children().removeClass("active");
            $("#all").addClass("active");
            setDataTableConfigAndEvents();
            $("#listAddSong").hide();
            $("#addSongPlaylist").hide();
        } else {
            console.log("no es correcto");
        }
    });
}

function play() {
    $("#play").hide();
    $("#pause").show();
    audio.play();
}

function pause() {
    $("#pause").hide();
    $("#play").show();
    audio.pause();
}

function loadPlaylist(id) {
    $.ajax({
        type: "GET",
        url: "/playlist/" + id + "/songs",
        success: function (data) {
            if (!data.includes("<html>")) {
                $("#divListSong").html(data);
                audio.pause();
                $("#player").attr("src", "");
                setDataTableConfigAndEvents();
                reloadArraySongs();
                if (data.includes("td")) {
                    $("#player").trigger("ended");
                }
                if (id > 0) {
                    ajaxSelect(id);
                } else {
                    $("#listAddSong").hide();
                    $("#addSongPlaylist").hide();
                }
            } else {
                console.log("no es correcto");
            }
        }
    });
}

function ajaxSelect(id) {
    $.ajax({
        type: "GET",
        url: "/playlist/" + id + "/not-songs",
        success: function (response) {
            loadSelect(response)
        }
    });
}

function loadSelect(listSong) {
    var select = $("#listAddSong");
    select.html('')
    if (listSong.length != 0) {
        select.append('<option value="0"></option>');
        $.each(listSong, function (index, value) {
            if (value.status) {
                select.append('<option value="' + value.id + '">' + value.name.replace('.mp3', '') + '</option>');
            }
        });
        $("#listAddSong").show();
        $("#addSongPlaylist").show();
    } else {
        $("#listAddSong").hide();
        $("#addSongPlaylist").hide();
    }
}

function setPlaylistEvent() {
    $("#listPlaylist").children().on("click", function () {
        $("#listPlaylist").children().removeClass("active");
        $(this).addClass("active");
        loadPlaylist($(this).data("id"))
    })
}

function setEvents() {

    $("#savePlaylist").on("click", function () {
        var name = $("#newPlaylist").val().trim();
        if (name != 0) {
            var length = $("#listPlaylist").find("[data-value='" + name + "']").length
            if (length == 0) {
                $.ajax({
                    type: "post",
                    url: "/playlist/addPlaylist",
                    contentType: "application/json",
                    data: JSON.stringify({
                        name: name,
                        songs: []
                    }),
                    dataType: "json",
                    success: function (data) {
                        $("#listPlaylist").append("<li class=\"list-group-item\" data-value=\"" + data.name + "\" data-id=\"" + data.id + "\">" + data.name + "</li>")
                        $("#newPlaylist").val('');
                        reloadArraySongs();
                        setPlaylistEvent()
                    }
                });
            } else {
                console.log("ya existe")
            }
        } else {
            console.log("campo vacio")
        }
    })

    $("#repeatPlaylist").on("click", function () {
        $(this).toggleClass("active");
    });

    $("#addSongPlaylist").on("click", function () {
        var playlist = $("#listPlaylist").find(".active").data('id')
        var song = $("#listAddSong").val();
        if (song.length != 0) {
            $.ajax({
                type: "POST",
                url: "/playlist/" + playlist + "/add-song",
                data: {
                    song: song
                },

                success: function (data) {

                    if (!data.includes("<html>")) {
                        $("#divListSong").html(data);
                        setDataTableConfigAndEvents()
                        ajaxSelect(playlist);
                        reloadArraySongs();
                        if (data.includes("td") && $("#player").attr("src").length == 0) {
                            $("#player").trigger("ended");
                        }
                    }
                },
                error: function (error) {
                    console.log(error);

                }
            });
        }
    });

    $("#player").on("ended", function () {
        if (!$("#repeatSong").hasClass("active")) {

            if (numberSongs.length > currentSong + 1) {
                currentSong++;
                if (!$("#randomSong").hasClass("active")) {
                    if ($(numberSongs[currentSong]).find("[class='oi oi-check']").length != 0) {
                        var name = $(numberSongs[currentSong]).data("name");
                        var id = $(numberSongs[currentSong]).data("id");
                        $("#player").attr("src", "files/" + id);
                        $("#currentSong").text(name);
                        play();
                    } else {
                        $("#player").trigger("ended");
                    }
                }else{
                    if ($(randomSongs[currentSong]).find("[class='oi oi-check']").length != 0) {
                        var name = $(randomSongs[currentSong]).data("name");
                        var id = $(numberSongs[currentSong]).data("id");
                        $("#player").attr("src", "files/" + id);
                        $("#currentSong").text(name);
                        play();
                    } else {
                        $("#player").trigger("ended");
                    }
                }
            } else if (numberSongs.length == currentSong + 1 && $("#repeatPlaylist").hasClass("active")) {
                if($("#randomSong").hasClass("active")){
                    generateRandom();
                }
                currentSong = -1;
                $("#player").trigger("ended");
            }

        }
    });

    $('input[type="range"]').on("input", function () {
        var val = ($(this).val() - $(this).attr('min')) / ($(this).attr('max') - $(this).attr('min'));
        $(this).css('background-image',
            '-webkit-gradient(linear, left top, right top, ' +
            'color-stop(' + val + ', #6AC0F7), ' +
            'color-stop(' + val + ', #c0c0c0)' +
            ')');
    })

    $("#duration").on("change", function () {
        audio.currentTime = $(this).val();
    });

    $("#volume").on("input", function () {
        audio.volume = $(this).val() / 100;
    });

    $("#player").on("durationchange", function () {
        var time = getTime(audio.duration);
        $("#totalTime").text(time);
        $("#duration").attr("max", audio.duration);
    });

    $("#player").on("seeking", function () {
        console.log("seeking");
    });

    $("#player").on("seeked", function () {
        console.log("seeked");
    });

    $("#player").on("timeupdate", function () {
        var time = getTime(audio.currentTime);
        $("#currentTime").text(time);
        $("#duration").val(audio.currentTime)
        $("#duration").trigger("input");
    });

    $("#play").on("click", play);

    $("#pause").on("click", pause);


}

function generateRandom() {
	randomSongs = [];
    var copy = numberSongs.slice(0);
    while (copy.length > 0) {
        var random = parseInt(Math.random() * copy.length);
        randomSongs.push(copy[random]);
        copy.splice(random, 1);
    }
}

function setDataTableConfigAndEvents() {
    $("#randomSong").on("click", function () {
        $("#randomSong").toggleClass("active")
        if ($("#randomSong").hasClass("active")) {
            generateRandom();
        }
        currentSong=-1;
        $("#player").trigger("ended");
    });

    $('#tableSong').DataTable({
        "scrollY": "251px",
        "scrollCollapse": false,
        "paging": false,
        "ordering": false,
        "info": false,
        "dom": '<"top">t<"bottom"f>'
    });

    $("#repeatSong").on("click", function () {
        $(this).toggleClass("active");
        if ($(this).hasClass("active")) {
            $("#player").prop("loop", true);
        } else {
            $("#player").prop("loop", false);
        }
    });

    $("[data-function='playMusic']").on("click", function () {
        var tr = $(this).parent();
        var id = tr.data("id");
        var name = tr.data("name");
        var index = numberSongs.indexOf(tr[0]);
        $("#player").attr("src", "files/" + id);
        play();
        $("#currentSong").text(name);
        currentSong = index;
    });

    $('#file').on('change', function () {

        if (this.files.length > 0) {
            if (this.files.length < 30) {
                if (100000000 > Object.values(this.files).map(e => e.size).reduce((e, a) => e + a)) {
                    $("#uploadFile").submit();
                } else {
                    buildModal(modalType.info, 'El maximo total a subir supera los 100Mb', "ERROR", 'danger');
                    $("#modal").modal('toggle');
                }
            } else {
                buildModal(modalType.info, 'No puede superar los 30 archivos', "ERROR", 'danger');
                $("#modal").modal('toggle');
            }

        }

    });

    $("[data-function='quitSongPlaylist']").on("click", function () {
        var song = $(this).parent().data("id");
        var playlist = $("#listPlaylist").find(".active").data('id');

        $.ajax({
            type: "post",
            url: "/playlist/" + playlist + "/quit-song",
            data: {
                song: song
            },
            success: function (data) {
                if (!data.includes("<html>")) {
                    $("#divListSong").html(data);
                    var current = currentSong;
                    var tr = numberSongs[currentSong];
                    reloadArraySongs();
                    currentSong = current;
                    if (currentSong == numberSongs.length) {
                        currentSong = -1;
                        if (numberSongs.length > 0) {
                            $("#player").trigger("ended")
                        } else {
                            pause()
                            $("#player").attr("src", "");
                            $("#currentTime").text('00:00');
                            $("#totalTime").text('00:00');
                            $("#currentSong").text('');
                        }
                    } else if ($(tr).data("id") == song) {
                        currentSong--;
                        $("#player").trigger("ended");
                    }
                    ajaxSelect(playlist);
                    setDataTableConfigAndEvents();
                }
            }
        });
    });

    $("[data-function='deleteSong']").on("click", function () {
        var id = $(this).parent().data("id");
        buildModal(modalType.confirmation, '¿Borrar esta cancion?', 'Borrar Cancion');
        $("#modal").modal('toggle');
        $('#ok').on("click", function () {
            location.href = "/song/" + id + "/delete-song"
        })
    })

    $("#btnUpload1").on("click", function () {
        $("#labelUpload").click();
    });

    $("#sort").on("click", function () {
        buildModal(modalType.orderList, '', 'ordenar');
        $("#modal").modal('toggle');
        setEventSortList()
        $("#saveSort").on("click", function () {
            var playlist = $("#listPlaylist").find(".active").data('id');
            var songList = $.map($("#sortList").find("li").toArray(), function (value, indexOrKey) {
                return {
                    id: $(value).children().data("song"),
                    sort: $(value).data("order")
                };

            });
            $.ajax({
                type: "POST",
                url: "playlist/" + playlist + "/sort-list",
                contentType: "application/json",
                data: JSON.stringify(songList),
                success: function (data) {
                    if (!data.includes("<html>")) {
                        $("#divListSong").html(data);
                        audio.pause();
                        $("#player").attr("src", "");
                        setDataTableConfigAndEvents();
                        reloadArraySongs();
                        if (data.includes("td")) {
                            $("#player").trigger("ended");
                        }
                        $("#modal").modal('toggle');
                    } else {
                        console.log("no es correcto");
                    }
                },
                error: function (error) {
                    console.log(error)
                }
            });
        });
    })

}

function setEventSortList() {
    $(".oi-arrow-top").off("click");

    $(".oi-arrow-bottom").off("click");

    $(".oi-arrow-top").on("click", function () {
        replacePosition(this, "top");
    });

    $(".oi-arrow-bottom").on("click", function () {
        replacePosition(this, "bottom");
    });
}

function replacePosition(button, type) {
    var actualPosition = $(button).parent().parent().parent().parent().parent()
    var newPosition = $("#sortList").find("li[data-order='" + (actualPosition.data("order") + (type == "bottom" ? 1 : -1)) + "']")
    if (newPosition.data("order") == (type == "bottom" ? numberSongs.length : 1)) {
        $(button).prop("hidden", true);
        newPosition.find(type == "bottom" ? ".oi-arrow-bottom" : ".oi-arrow-top").prop("hidden", false);
    } else if (actualPosition.data("order") == (type == "bottom" ? 1 : numberSongs.length)) {
        actualPosition.find(type == "bottom" ? ".oi-arrow-top" : ".oi-arrow-bottom").prop("hidden", false);
        newPosition.find(type == "bottom" ? ".oi-arrow-top" : ".oi-arrow-bottom").prop("hidden", true);
    }
    var html = actualPosition.html();
    actualPosition.html(newPosition.html());
    newPosition.html(html);
    setEventSortList();
}

function buildModal(type, text, title, typeText) {
    var modal = '';
    modal += '<div class="modal-dialog modal-dialog-centered">';
    modal += '<div class="modal-content">';
    switch (type) {
        case modalType.confirmation:
            modal += '<div class="modal-header"><h5 class="modal-title">' + title + '</h5></div>';
            modal += '<div class="modal-body">' + text + '</div>';
            modal += '<div class="modal-footer">';
            modal += '<button type="button" id="ok" class="btn btn-primary">OK</button>';
            modal += '<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancelar</button></div>';
            break;
        case modalType.info:
            modal += '<div class="modal-header"><h5 class="modal-title text-' + typeText + '">' + title + '</h5></div>';
            modal += '<div class="modal-body">' + text + '</div>';
            modal += '<div class="modal-footer">';
            modal += '<button type="button" class="btn btn-secondary" data-dismiss="modal">OK</button></div>';
            break;
        case modalType.orderList:
            modal += '<div class="modal-header"><h5 class="modal-title text-' + typeText + '">' + title + '</h5></div>';
            modal += '<div class="modal-body">';
            modal += '<ul class="list-group" id="sortList">';
            for (var i = 0; i < numberSongs.length; i++) {
                modal += '<li class="list-group-item list-group-flush" data-order="' + (i + 1) + '"><div data-song=' + $(numberSongs[i]).data('id') + ' class="row">';
                modal += '<div class="col-9">' + $(numberSongs[i]).data('name').replace('.mp3', '') + '</div>';

                modal += '<div class="col-3"><div class="row"><div class="col-6">'
                var hide = 'hidden'
                if (i != 0) {
                    hide = '';
                }
                modal += '<button ' + hide + ' class="btn btn-sm btn-success oi oi-arrow-top"></button>'
                modal += '</div><div class="col-6">'
                hide = 'hidden'
                if (i + 1 != numberSongs.length) {
                    hide = '';
                }
                modal += '<button ' + hide + ' class="btn btn-sm btn-success oi oi-arrow-bottom"></button>'
                modal += '</div></div></div></li>'
            }
            modal += '</ul></div>';
            modal += '<div class="modal-footer">';
            modal += '<button type="button" id="saveSort" class="btn btn-primary">Guardar</button>'
            modal += '<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancelar</button></div>';

            break;

        default:
            break;
    }

    modal += '</div></div>';
    $("#modal").html(modal);
}