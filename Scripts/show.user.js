// ==UserScript==
// @name           DS Workbench Scripts
// @namespace      none
// @include        http://de*.die-staemme.de/game.php?*screen=overview*
// @include        http://de*.die-staemme.de/game.php?screen=overview*
// @include        http://de*.die-staemme.de/game.php?*screen=overview
// @include        http://de*.die-staemme.de/game.php?*screen=place*
// @include        http://de*.die-staemme.de/game.php?screen=place*
// @include        http://de*.die-staemme.de/game.php?*screen=place
// ==UserScript==

(
function() {
doImport('http://www.dsworkbench.de/DSWorkbench/export/mootools.js');
doImport('http://www.dsworkbench.de/DSWorkbench/export/countdown2.js');
doImport('http://www.dsworkbench.de/DSWorkbench/export/sprintf.js');
doImport('http://www.dsworkbench.de/DSWorkbench/export/mooRainbow2.js');
//includeJavascript('http://www.dsworkbench.de/DSWorkbench/export/mooRainbow2.js');
//includeJavascript('countdown.js');

if(window.navigator.userAgent.indexOf("Firefox") > -1){
	window.addEventListener('load', function(){doAction();}, false);
}else{
	addLoadEvent(function(){doAction();});
}
}
)()

function doImport(src) {
	if (document.createElement && document.getElementsByTagName) {
		var head_tag = document.getElementsByTagName('head')[0];
		var script_tag = document.createElement('script');
		script_tag.setAttribute('type', 'text/javascript');
		script_tag.setAttribute('src', src);
		head_tag.appendChild(script_tag);
	}
}



var attacks = new Array({
		'type':0,
		'source':111217,
		'target':123456,
		'unit':0,
		'send':'27.09.2009 02:00:00',
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
	
function initRainbow() {
	var r = new MooRainbow('colorPicker', {
		id: 'colorPicker1',
		wheel: true,
		'onComplete': function(color) {
			insBB('color',color.hex);
		}
	});
	
	var ccfg = {
		'countplus': false,
		'days': true,
		'formatDays': '%01d Tag(e), ',
		'formatHours': '%02dh ',
		'formatMinutes': '%02dm ',
		'formatSeconds': '%02ds',
		'message': 'Abgelaufen',
		'onComplete': function() { colorize(this); },
		'onTick': function(t, r) { colorize(this,r) }

	};
	
	function colorize(obj, r) {
		if(!r){
			 r=0;
		}
		if(r > 600){
				obj.el.setStyle('color','rgb(0,200,0)');
		}else{
			obj.el.setStyle('color','rgb(255,'+Math.round(r<1?0:(0.3*r)) +',0)');
		}
	}
	
	$$('.countdown').each(function(item, index){
		var dv = item.title.match(/([0-9]{1,2}).([0-9]{1,2}).([0-9]{2,4})( ([0-9]{1,2}).([0-9]{1,2})(.([0-9]{1,2}))?)?/);
		new Countdown(item, new Date(dv[3]>99?dv[3]:2000+(dv[3]*1),dv[2]-1,dv[1],(dv[5]>0?dv[5]:0),(dv[6]>0?dv[6]:0),(dv[8]>0?dv[8]:0)),ccfg);
	});
};


	function doAction() {
		var formNode = document.getElementsByName("units")[0];
		setTitle(formNode);
		formNode.appendChild(buildTable(getVillageID(formNode)));
	}
	
	function setTitle(formNode){
	var titleNode = document.createElement("h3");
	titleNode.appendChild(document.createTextNode('Geplante Angriffe'));
	formNode.appendChild(titleNode);
	}
	
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
			var unitNode =  document.createElement("td");
				var img = document.createElement("img");
				img.setAttribute('src', 'graphic/unit/unit_ram.png?1');
				img.setAttribute('title', 'Rammbock');
				img.setAttribute('alt', '');
				unitNode.appendChild(img);
				line.appendChild(unitNode);
			var sendNode = document.createElement("td");
			sendNode.setAttribute('class', 'countdown');
			sendNode.setAttribute('title', attacks[i].send);
			sendNode.appendChild(document.createTextNode('countdown'));
			line.appendChild(sendNode);
				var arriveNode = document.createElement("td");
				arriveNode.appendChild(document.createTextNode(attacks[i].arrive));
				line.appendChild(arriveNode);
				body.appendChild(line);
		}
		}
	}
	
	
	function getVillageID(formNode){
		var villageURL = formNode.getAttribute('action');
		var idStart = villageURL.indexOf('village=') + 'village='.length;
		var idEnd = villageURL.indexOf('&', idStart);
		return villageURL.substring(idStart, idEnd);
	}
	
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
      initRainbow();
    }
  } else {
    window.onload = function() {
      if (oldonload) {
        oldonload();
      }
      initRainbow();
      func();
    }
  }
}

/**********HELPER FUNCTIONS***********/

function getValues(obj){
	 var res = '';

	res += 'Objekt: '+obj+'\n\n';
 	for(temp in obj)
 	{
 		res += temp +': '+obj[temp]+'\n';
	}
	alert(res);
} 