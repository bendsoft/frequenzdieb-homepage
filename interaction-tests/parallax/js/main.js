var mouse = {
  x: 0,
  y: 0
}

var scroll = {
  y: 0,
  pages: 0
}

$(document).ready(function() {
  $(window).on("mousemove", e => {
    mouse.x = (e.pageX / window.innerWidth) - 0.5;
    mouse.y = (e.pageY / -window.innerHeight) - 0.5;
    $(".layer.one .content").css("transform", "translate(" + (-mouse.x * 20) + "%, " + (mouse.y * 15) + "%)");
    $(".layer.two .content").css("transform", "translate(" + (-mouse.x * 15) + "%, " + (mouse.y * 12) + "%)");
    $(".layer.three .content").css("transform", "translate(" + (-mouse.x * 10) + "%, " + (mouse.y * 9) + "%)");
    $(".layer.four .content").css("transform", "translate(" + (-mouse.x * 5) + "%, " + (mouse.y * 6) + "%)");
  });
  $(window).on("scroll", e => {
    e.preventDefault();
    scroll.y = $(window)[0].scrollY;
    scroll.pages = scroll.y / window.innerHeight;
    console.log(scroll.pages);
    $(".splash").css({
      "height": 120 - (40 * scroll.pages) + "%",
      "filter": "brightness(" + (1 - scroll.pages * 2) + ")"
    });
  });
  $(".layer.one").on("click", e => {
    $(window)[0].scrollTo(0, window.innerHeight);
  });
  $(".layer.two").on("click", e => {
    $
  })
});
