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
    console.log("next");
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

    console.log(scheduleNum);

    if ($(scheduleNum).length) {
        $(".schedule").hide();

        $(scheduleNum).show();
        $(scheduleNum).attr('id', 'current');
        if (prev !== null) {
            prev.attr('id', '');
        }
    }


}