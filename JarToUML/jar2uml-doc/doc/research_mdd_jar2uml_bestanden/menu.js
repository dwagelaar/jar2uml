/**
 * This prints the switch to toggle a menu
 */
function showMenuToggle(showtxt,hidetxt,menu) {
  if(document.getElementById) {
		show = '<img src="research_mdd_jar2uml_bestanden/arrow_down.gif" alt="'+showtxt+'">';
		hide = '<img src="research_mdd_jar2uml_bestanden/arrow_up.gif" alt="'+hidetxt+'">';

    document.writeln('<div class=\'toctoggle\'><a href="javascript:toggleMenu(\'' + menu + '\')" class="toc">' +
    '<span id="' + menu + '_showlink" style="display:none;">' + show + '</span>' +
    '<span id="' + menu + '_hidelink">' + hide + '</span>'
    + '</a></div>');
  }
}

/**
 * This toggles the visibility of a menu
 */
function toggleMenu(menu) {
  var toc = document.getElementById(menu);
  var showlink=document.getElementById(menu + '_showlink');
  var hidelink=document.getElementById(menu + '_hidelink');
  if(toc.style.display == 'none') {
    toc.style.display = stateVar;
    hidelink.style.display='';
    showlink.style.display='none';
  } else {
    stateVar = toc.style.display;
    toc.style.display = 'none';
    hidelink.style.display='none';
    showlink.style.display='';

  }
}
