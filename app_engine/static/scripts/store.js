function hideAllSchedules() {
    $(".schedule").hide();
}

function init() {
    hideAllSchedules();
    showSchedule(null, 0);
}

$(document).ready(init);

$("#nextButton").click(function () {
    changeSchedule(1);
});
$("#prevButton").click(function () {
    changeSchedule(-1);
});

function changeSchedule(index) {
    //console.log("next");
    var currentSchedule = $("#current");

    var scheduleClass = currentSchedule.attr('class').split(/\s+/)[1];

    if (scheduleClass !== undefined) {
        var scheduleNum = parseInt(scheduleClass.substr(8));
        console.log(scheduleNum);

        if (scheduleNum + index >= 0) {
            showSchedule(currentSchedule, scheduleNum + index);
        }
    }
}

function showSchedule(prev, i) {
    var scheduleNum = '.schedule' + i;

    // console.log(scheduleNum);

    if ($(scheduleNum).length) {
        $(".schedule").hide();

        $(scheduleNum).show();
        $(scheduleNum).attr('id', 'current');
        if (prev !== null) {
            prev.attr('id', '');
        }
    }
}

$("#addScheduleForm").validate({
    submitHandler: function (form) {
        $('#addScheduleForm').hide();
        $("#progressBar").show(500).css("display", "block");

        var params = {};

        params.store_name = $("#scheduleStoreName").val();
        params.dep_name = $("#scheduleDepName").val();
        params.store_user_id = $("#scheduleStoreUserId").val();
        params.year = $("#scheduleYear").val();
        params.week = $("#scheduleWeek").val();
        params.week_offset = $("#scheduleWeekOffset").val();
        params.file = $("#scheduleFile")[0].files[0];

        // WILL NOT WORK ON IE <10
        var data = new FormData();

        $.each(params, function(key, val) {
            data.append(key, val);
        });

        $.ajax(
            {
                xhr: function () {
                    var xhr = new window.XMLHttpRequest();
                    xhr.upload.addEventListener("progress", function (evt) {
                        if (evt.lengthComputable) {
                            var percentComplete = parseInt((evt.loaded / evt.total) * 100);
                            console.log(percentComplete);
                            $('#progressBar').attr('value', percentComplete);
                            //Do something with upload progress here
                        }
                    }, false);

                    xhr.addEventListener("progress", function (evt) {
                        if (evt.lengthComputable) {
                            var percentComplete = evt.loaded / evt.total;
                            console.log(percentComplete);
                            //Do something with download progress
                        }
                    }, false);

                    return xhr;
                },
                type: "POST",
                url: $("#addScheduleForm").attr('action'),
                contentType: false,
                data: data,
                processData: false
            }).done(function (data) {
                console.log(data);

                alert(data.desc);
                if (data.code == 0) {
                    location.reload();
                } else {
                    $('#progressBar').hide();
                    $('#addScheduleForm').show();
                }
            }).fail(function (data) {
                alert("Schedule Add failed: " + data.statusText);
                console.log(data);
                $('#progressBar').hide();
                $('#addScheduleForm').show();
            }
        );
    }
});

$("#addStoreForm").validate({
    submitHandler: function (form) {
        var params = {};

        params.store_name = $("#store_name").val();
        params.dep_name = $("#dep_name").val();

        $.ajax(
            {
                type: "POST",
                url: $("#addStoreForm").attr('action'),
                data: params
            }).done(function (data) {
                console.log(data);

                alert(data.desc);
                if (data.code == 0) {
                    location.reload();
                }
            }).fail(function (data) {
                alert("Store Add failed: " + data.statusText);
                console.log(data);
            }
        );
    }
});