function stylizeLinks() { 
   aL = document.getElementsByTagName('a');  
   for (i=0;i<aL.length;i++)
   	  if
	  	(aL[i].href.indexOf('pdf') > -1) {
			aL[i].className += ' pdf';
		}
	  /*else if
	  	(aL[i].href.indexOf('doc') > -1) {
			aL[i].className += ' doc';
		}*/
	  else if
	  	(aL[i].href.substring(0,6) == 'mailto') {
			aL[i].className += ' email';
		}
	  else if
	  	((aL[i].href.indexOf('.jpg') > -1) ||
		(aL[i].href.indexOf('.gif') > -1)) {
			aL[i].target = '_blank';
		}
      else if ((aL[i] != '' ) && 
         (aL[i].hostname != location.hostname) &&
		 (aL[i].hostname.indexOf('aiv') < 0) &&
		 (aL[i].href.indexOf('vub.ac.be') < 0) &&
         (aL[i].href.substring(0,10) != 'javascript') &&
		 (aL[i].href.substring(0,6) != 'mailto')) {
            aL[i].target = '_blank';
      }
}


