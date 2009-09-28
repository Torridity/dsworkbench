// ==UserScript==
// @name           DS Workbench Scripts
// @namespace      none
// @include        http://de*.die-staemme.de/game.php?*screen=overview*
// @include        http://de*.die-staemme.de/game.php?screen=overview*
// @include        http://de*.die-staemme.de/game.php?*screen=overview
// @include        http://de*.die-staemme.de/game.php?*screen=place*
// @include        http://de*.die-staemme.de/game.php?screen=place*
// @include        http://de*.die-staemme.de/game.php?*screen=place
// ==/UserScript==

/**Action event, executed if DOM has loaded*/
if(window.navigator.userAgent.indexOf("Firefox") > -1){
	window.addEventListener('load', function(){doAction();}, false);
}else{
	addLoadEvent(function(){doAction();});
}

/**List of exported attacks*/
var attacks = new Array({
		'type':0,
		'source':111217,
		'target':123456,
		'unit':0,
		'send':'02:00:00',
		'arrive':'27.09.2009 12:00:00'
		},
		{
		'type':0,
		'source':111217,
		'target':123456,
		'unit':0,
		'send':'27.09.2009 02:00:00',
		'arrive':'27.09.2009 12:00:00'
		},
		{
		'type':2,
		'source':104232,
		'target':123456,
		'unit':0,
		'send':'27.09.2009 02:00:00',
		'arrive':'27.09.2009 12:00:00'
		}
	);
	

	/**Building additional document data*/
	function doAction() {
		//Place processing
		var formNode = document.getElementsByName("units")[0];
		var titleNode = document.createElement("h3");
		titleNode.appendChild(document.createTextNode('Geplante Angriffe'));
		formNode.appendChild(titleNode);
		formNode.appendChild(buildTable(getVillageID(formNode)));
		//View processing
	}
	
	/**Build planned attacks table for current village*/
	function buildTable(villageID){
		var tab = document.createElement("table");
		var body = document.createElement("tbody");
		tab.appendChild(body);
		tab.setAttribute('class', 'vis');
		var header = document.createElement("tr")
		var cols = new Array({
			'name':'Typ',
			'width':30
			},
			{
			'name':'Ziel',
			'width':250
			},
			{
			'name':'Einheit',
			'width':30
			},
			{
			'name':'Start',
			'width':170
			},
			{
			'name':'Ankunft',
			'width':170
			});
			
		for(var c = 0;c<cols.length;c++){
			var colNode = document.createElement("th");
			colNode.setAttribute('width', cols[c].width);
			colNode.appendChild(document.createTextNode(cols[c].name));
			header.appendChild(colNode);
		}	
		body.appendChild(header);
		buildRows(body, villageID);
		return tab;
	}
	
	/**Insert all rows into the table*/
	function buildRows(body, villageID){
		for (var i = 0; i < attacks.length; i++){
			if(attacks[i].source == villageID){
			var line = document.createElement("tr");
			var rowData = '';
			var typeNode = document.createElement("td");
			if(attacks[i].type == 0){
				var img = document.createElement("img");
				img.setAttribute('src', 'http://www.dsworkbench.de/DSWorkbench/export/fake.png');
				img.setAttribute('title', 'Fake');
				img.setAttribute('alt', '');
				typeNode.appendChild(img);
		  }else{
		  	//rowData +=  "<td>-</td>";
		  	typeNode.appendChild(document.createTextNode('-'));
			}
			line.appendChild(typeNode);
			var targetNode = document.createElement("td");
			targetNode.appendChild(document.createTextNode(attacks[i].target));
			line.appendChild(targetNode);
			//rowData += "<td>" + attacks[i].target + "</td>";
			var unitNode =  document.createElement("td");
			var img = document.createElement("img");
			img.setAttribute('src', 'graphic/unit/unit_ram.png?1');
			img.setAttribute('title', 'Rammbock');
			img.setAttribute('alt', '');
			unitNode.appendChild(img);
			line.appendChild(unitNode);
			var sendNode = document.createElement("td");
		
			var spanNode = document.createElement("span");
			spanNode.setAttribute('class', 'timer');
			spanNode.appendChild(document.createTextNode('Warte...'));
			var altNode = document.createElement("td");
			var tn = document.createTextNode('Abgelaufen');
			altNode.appendChild(tn);
			altNode.setAttribute('style', 'display:none');
			altNode.setAttribute('class', 'warn');
			sendNode.appendChild(spanNode);
			/**Set send time in seconds relative to current time*/
			var sendTime = 23*60*60 + 20*60;
			sendTime += 15*60 + 50;
			//var serverTime = getTime(document.getElementById("serverTime"));
			//var startTime = getTime(spanNode);
			addTimer(spanNode, sendTime, false);
			line.appendChild(sendNode);
			line.appendChild(altNode);
		
			var arriveNode = document.createElement("td");
			arriveNode.appendChild(document.createTextNode(attacks[i].arrive));
			line.appendChild(arriveNode);
			body.appendChild(line);
			}
		}
	}
	
 
 /*  // Function to add event listener to t
   function load() { 
     var el = document.getElementById("t"); 
     el.addEventListener("click", modifyText, false); 
   } */
	/**********HELPER FUNCTIONS***********/
	/**Get ID of current village*/
	function getVillageID(formNode){
		var villageURL = formNode.getAttribute('action');
		var idStart = villageURL.indexOf('village=') + 'village='.length;
		var idEnd = villageURL.indexOf('&', idStart);
		return villageURL.substring(idStart, idEnd);
	}
	
	/**Adding function to onload listener*/
	function addLoadEvent(func) {
  	var oldonload;
		if(window.navigator.userAgent.indexOf("Firefox") > -1){
			oldonload = unsafeWindow.onload;
		}else{
			oldonload = window.onload
		}

  	if (typeof window.onload != 'function') {
  	  window.onload = function() {
      func();
    	}
 	 } else {
    	window.onload = function() {
      if (oldonload) {
        oldonload();
      }
      func();
    	}
  	}
	}

	/**For debugging only*/
	function getValues(obj){
		var res = '';

		res += 'Objekt: '+obj+'\n\n';
 		for(temp in obj) {
 			res += temp +': '+obj[temp]+'\n';
		}
		alert(res);
} 