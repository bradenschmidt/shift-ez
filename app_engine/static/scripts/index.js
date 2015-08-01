function hideAllSchedules() {
    $(".schedules").hide()
}

function initializeFunction() {
    hideAllSchedules();
    showStoreSchedulesByIndex(0);
}

$(document).ready(initializeFunction);

$(".store").click(function () {
    hideAllSchedules();

    var store = $(this).attr('id');

    var storeNum = store.substr(5);

    showStoreSchedulesByIndex(storeNum)
});

function showStoreSchedulesByIndex(storeNum) {
    var scheduleNum = '#schedules' + storeNum;

    $(scheduleNum).show()
}