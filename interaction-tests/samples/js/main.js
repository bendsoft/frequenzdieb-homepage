$(document).ready(function() {
  $(".box:not(.space)").on("mouseenter", function (e) {
    if (!$(this).children("audio")[0].loop) {
      $(this).children("audio")[0].play();
    }

  })
  $(".box").on("click", function (e) {  
    $(this).toggleClass("active")
    console.log($(this).children("audio"));
    if ($(this).children("audio")[0].loop) {
      $(this).children("audio")[0].loop = false;
    } else {
      $(this).children("audio")[0].loop = true;
      $(this).children("audio")[0].play();
    }
  })
  // $(".box:not(.space)").on("mouseleave", function (e) {
  //   $(this).children("audio")[0].pause();
  // })
});
