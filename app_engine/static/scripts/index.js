function hideAllSchedules() {
    $(".schedules").hide()
}
$(document).ready(hideAllSchedules());

$(".store").click(function () {
    hideAllSchedules();

    var store = $(this).attr('id');

    var storeNum = store.substr(5);

    var scheduleNum = '#schedules' + storeNum;

    $(scheduleNum).show()
});