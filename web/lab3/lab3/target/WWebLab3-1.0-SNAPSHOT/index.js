function add_row_end(x,y,r,msg)
{
    $("#res_table")[0].innerHTML+=`<tr class="add_row">\n` +
        `                        <td>${x}</td>\n` +
        `                        <td>${y}</td>\n` +
        `                        <td>${r}</td>\n` +
        `                        <td>${msg}</td>\n` +
        `                        </tr>`;
}
function add_point(x,y,r,res)
{
    let cur_r = Number($(".sp_r > .ui-spinner-input")[0].value.replace(',','.'))
    if(cur_r ==0)cur_r = 1;
    let x_r = (x/cur_r)*160+200
    let y_r = (-y/cur_r)*160+200
    if(r !== cur_r)
    {
        $("#img")[0].innerHTML+=`<circle cx="${x_r}" cy="${y_r}" r="2" class = "add_point" fill="orange" stroke="orange"/>`;
    }
    else{
        let color = res ? "green" : "red"
        $("#img")[0].innerHTML+=`<circle cx="${x_r}" cy="${y_r}" r="2" class = "add_point" fill="${color}" stroke="${color}"/>`;
    }
}
function clear_row_points()
{
    $(".add_row").remove()
    $(".add_point").remove()
}
function show_res(s)
{
    $(".row_res").removeAttr("hidden").value = s
}
function hide_res()
{
    $(".row_res").attr("hidden", "");
}
function change_r()
{
    let val = Number($(".sp_r > .ui-spinner-input")[0].value.replace(',','.'))
    if(val!=="")
    {
        $(".graph_R").text(val)
        $(".graph_R2").text(val/2)
        $(".graph_MR").text(-val)
        $(".graph_MR2").text(-val/2)
    }
    else {
        $(".graph_R").text("R")
        $(".graph_R2").text("R/2")
        $(".graph_MR").text("-R")
        $(".graph_MR2").text("-R/2")
    }
}
function update(points)
{
    //hide_res()
    clear_row_points()
    change_r()
    for (key in points.data)
    {
        let el = points.data[key];
        add_row_end(el.x,el.y,el.r,el.res ? "Попал!" : "Не попал!")
        add_point(el.x,el.y,el.r,el.res)
    }
}
function fill_form(x, y, r)
{
    let x_inp = $(".sp_x > .ui-spinner-input")[0]
    let y_inp = $(".com_y")[0]
    x_inp.value = x
    y_inp.value = y
    $(".frm_submit")[0].click()
}
$(function(){
    $("#img").click(function(event){
        let r_inp = $(".sp_r > .ui-spinner-input")[0]
        if(r_inp.value === "")
        {
            show_res("R не выбран!")
        }
        else
        {
            var pt = this.createSVGPoint()
            pt.x = event.clientX
            pt.y = event.clientY
            var cursorpt = pt.matrixTransform(this.getScreenCTM().inverse())
            let [x,y] = [cursorpt.x,cursorpt.y]
            x=(x-200)/160;
            y=-(y-200)/160;
            let r = Number(r_inp.value.replace(',','.'))
            x*=r
            y*=r
            x = x.toFixed (3);
            y = y.toFixed (3);
            fill_form(x,y, r)
        }
    });
});
