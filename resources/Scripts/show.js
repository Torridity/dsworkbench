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
		'arrive':'27.09.2009 12:00:00',
		'finished':1255168681
		},
		{
		'type':0,
		'source':111217,
		'target':123456,
		'unit':0,
		'send':'27.09.2009 02:00:00',
		'arrive':'27.09.2009 12:00:00',
		'finished':1255168681
		},
		{
		'type':2,
		'source':104232,
		'target':123456,
		'unit':0,
		'send':'27.09.2009 02:00:00',
		'arrive':'27.09.2009 12:00:00',
		'finished':1255168681
		}
	);
	

	/**Building additional document data*/
	function doAction() {
		//Place processing
		var formNode = document.getElementsByName("units")[0];
		if(formNode != null && !isSimulator(formNode)){
			//place view
			var titleNode = document.createElement("h3");
			titleNode.appendChild(document.createTextNode('Geplante Angriffe'));
			formNode.appendChild(titleNode);
			formNode.appendChild(buildTable(getVillageID(formNode)));
		}else{
			//overview
			modifyOverviewTable();
		}
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
				var altNode = document.createElement("td");
				var tn = document.createTextNode('Abgelaufen');
				altNode.appendChild(tn);
				
				altNode.setAttribute('class', 'warn');
				sendNode.appendChild(spanNode);
				/**Set send time in seconds relative to current time*/
				var sendTime = 20*60*60 + 34*60;
				//sendTime += 15*60 + 50;			
				if(getRealServerTime(document.getElementById('serverTime')) > attacks[i].finished){
					altNode.setAttribute('style', 'display:inline');
			}else{
				spanNode.setAttribute('class', 'timer');
				spanNode.appendChild(document.createTextNode('Warte...'));
				altNode.setAttribute('style', 'display:none');
				//alert(getRealServerTime(document.getElementById('serverTime')));
				//var serverTime = getTime(document.getElementById("serverTime"));
				//var startTime = getTime(spanNode);
				if(window.navigator.userAgent.indexOf("Firefox") > -1){
					unsafeWindow.addTimer(spanNode, sendTime, false);
				}else{
					addTimer(spanNode, sendTime, false);
				}
				line.appendChild(sendNode);
				}
				
				line.appendChild(altNode);
		
				var arriveNode = document.createElement("td");
				arriveNode.appendChild(document.createTextNode(attacks[i].arrive));
				line.appendChild(arriveNode);
				body.appendChild(line);
				}
			}
	}
	
	/**Check if simulator page is shown. If true do not show planned attacks*/
 	function isSimulator(formNode){
		var attrib = formNode.getAttribute('action');
		return (formNode.action.indexOf('mode=sim') > -1);
	}
	
	Array.prototype.contains = function (element) {
		for (var i = 0; i < this.length; i++) {
			if (this[i] == element) {
				return true;
			}
		}
		return false;
	}

 	function modifyOverviewTable(attack){
 		var doneElems = new Array();
 		var allElems = new Array();
 		var attackedElems = new Array();
 		var spans = document.getElementsByTagName('span');
 		for(var i = 0;i<spans.length;i++){
 			var attrib = spans[i].getAttribute('id');
 			
 			if(spans[i].id != null && spans[i].id.indexOf('label_')>-1 && spans[i].id.indexOf('label_text_') < 0){
 				allElems.push(attrib.replace('label_', ''));
 			}
 		}
 
 		for (var i = 0; i < attacks.length; i++){
			var node = document.getElementById('label_' + attacks[i].source);
			if(node != null && !doneElems.contains(attacks[i].source)){
				//element visible
				doneElems.push(attacks[i].source);
				attackedElems.push(attacks[i].source);
				var img = document.createElement("img");
				img.setAttribute('src', 'graphic/dots/green.png?1');
				img.setAttribute('title', 'attack');
				img.setAttribute('alt', '');
				node.insertBefore(img, node.getElementsByTagName('a')[0]);
		}
	}
	//alert(attackedElems);
	for (var i = 0; i < allElems.length; i++){
		if(!attackedElems.contains(allElems[i])){
			//alert('cont ' + allElems[i]);
			var node = document.getElementById('label_' + allElems[i]);
			var img = document.createElement("img");
			img.setAttribute('src', 'graphic/overview/prod_avail.png?1');
			img.setAttribute('title', 'attack');
			img.setAttribute('alt', '');
			node.insertBefore(img, node.getElementsByTagName('a')[0]);
		}
	}
}

	/**********HELPER FUNCTIONS***********/
	/**Get ID of current village*/
	function getVillageID(formNode){
		var villageURL = formNode.getAttribute('action');
		var idStart = villageURL.indexOf('village=') + 'village='.length;
		var idEnd = villageURL.indexOf('&', idStart);
		return villageURL.substring(idStart, idEnd);
	}
	
	function getRealServerTime(element) {
	currentDate = document.getElementById('serverDate').firstChild.nodeValue;
	splitDate = currentDate.split('/');
 
	date = splitDate[0];
	month = splitDate[1]-1;
	year = splitDate[2];
 
	// Zeit auslesen
	if(element.firstChild.nodeValue == null) return -1;
	var part = element.firstChild.nodeValue.split(":");
 
	// Führende Nullen entfernen
	for(var j=1; j<3; j++) {
		if(part[j].charAt(0) == "0")
			part[j] = part[j].substring(1, part[j].length);
	}
 
	// Zusammenfassen
	var hours = parseInt(part[0]);
	var minutes = parseInt(part[1]);
	var seconds = parseInt(part[2]);
	var time = hours*60*60+minutes*60+seconds;
	var dateObject = new Date(year, month, date,hours, minutes,seconds);
	return dateObject.getTime()/1000;
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