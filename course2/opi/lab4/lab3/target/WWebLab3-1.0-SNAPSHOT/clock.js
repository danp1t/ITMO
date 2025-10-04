const days = ["Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"];
const months = ["Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"];
$(function()
{
    update_clock()
    function update_clock()
    {
        let today = new Date();
        $("#clock")[0].innerHTML = `${today.getDate()} ${months[today.getMonth()]} ${today.getFullYear()}, ${days[today.getDay()]} \n ${today.getHours()}:${today.getMinutes()}:${today.getSeconds()}`
        setTimeout(update_clock,12000)
    }
})