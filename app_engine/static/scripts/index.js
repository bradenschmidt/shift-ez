function hideAllSchedules() {
    $(".schedules").hide();
    showSchedule(0);
}

function initializeFunction() {
    $(".schedule").hide();
    hideAllSchedules();
    showStoreSchedulesByIndex(0);
    showSchedule(null, 0);
}

$(document).ready(initializeFunction);

$(".store").click(function () {
    hideAllSchedules();

    var store = $(this).attr('id');

    if (store !== undefined) {
        var storeNum = store.substr(5);

        showStoreSchedulesByIndex(storeNum)
    }

});

function showStoreSchedulesByIndex(storeNum) {
    var scheduleNum = '.schedules' + storeNum;

    $("#wPaginate").wPaginate('total', 5);

    $(scheduleNum).show()
}

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

    if ($(scheduleNum).length) {
        $(".schedule").hide();

        console.log();

        $(scheduleNum).show();
        $(scheduleNum).attr('id', 'current');
        if (prev !== null) {
            prev.attr('id', '');
        }
    }


}