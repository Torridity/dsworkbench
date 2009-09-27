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

//doImport('http://www.dsworkbench.de/DSWorkbench/export/countdown2.js');
//includeJavascript('http://www.dsworkbench.de/DSWorkbench/export/mooRainbow2.js');
//includeJavascript('countdown.js');


function doImport(src) {
	if (document.createElement && document.getElementsByTagName) {
		var head_tag = document.getElementsByTagName('head')[0];
		var script_tag = document.createElement('script');
		script_tag.setAttribute('type', 'text/javascript');
		script_tag.setAttribute('src', src);
		head_tag.appendChild(script_tag);
	}
}

function addScripts() {
	var head_tag = document.getElementsByTagName('head')[0];

	var script_tag3 = document.createElement('script');
	script_tag3.setAttribute("type", "text/javascript");
	
	var data = "String.implement({sprintf:function(){if(typeof arguments=='undefined'){return this}if(arguments.length<1){return this}if(typeof RegExp=='undefined'){return this}var e=function(c,j){if(j){c.sign=''}else{c.sign=c.negative?'-':c.sign}var m=c.min-c.argument.length+1-c.sign.length;var g=new Array(m<0?0:m).join(c.pad);if(!c.left){if(c.pad=='0'||j){return c.sign+g+c.argument}else{return g+c.sign+c.argument}}else{if(c.pad=='0'||j){return c.sign+c.argument+g.replace(/0/g,' ')}else{return c.sign+c.argument+g}}};";
	data = data + "var k=new RegExp(/(%([%]|(\\\-)?(\\\+|\\\x20)?(0)?(\\\d+)?(\\\.(\\\d)?)?([bcdfosxX])))/g);"
	data = data + "var b=new Array();var f=new Array();var h=0;var n=0;var o=0;var l=0;var i='';var d=null;while(d=k.exec(this)){n=l;o=k.lastIndex-d[0].length;f[f.length]=this.substring(n,o);l=k.lastIndex;b[b.length]={match:d[0],left:d[3]?true:false,sign:d[4]||'',pad:d[5]||' ',min:d[6]||0,precision:d[8],code:d[9]||'%',negative:parseInt(arguments[h])<0?true:false,argument:String(arguments[h])};if(d[9]){h+=1}}f[f.length]=this.substring(l);if(b.length==0){return this}if((arguments.length)<h){return this}var p=null;var d=null;var a=null;for(a=0;a<b.length;a++){if(b[a].code=='%'){substitution='%'}else{if(b[a].code=='b'){b[a].argument=String(Math.abs(parseInt(b[a].argument)).toString(2));substitution=e(b[a],true)}else{if(b[a].code=='c'){b[a].argument=String(String.fromCharCode(parseInt(Math.abs(parseInt(b[a].argument)))));substitution=e(b[a],true)}else{if(b[a].code=='d'){b[a].argument=String(Math.abs(parseInt(b[a].argument)));substitution=e(b[a])}else{if(b[a].code=='f'){b[a].argument=String(Math.abs(parseFloat(b[a].argument)).toFixed(b[a].precision?b[a].precision:6));substitution=e(b[a])}else{if(b[a].code=='o'){b[a].argument=String(Math.abs(parseInt(b[a].argument)).toString(8));substitution=e(b[a])}else{if(b[a].code=='s'){b[a].argument=b[a].argument.substring(0,b[a].precision?b[a].precision:b[a].argument.length);substitution=e(b[a],true)}else{if(b[a].code=='x'){b[a].argument=String(Math.abs(parseInt(b[a].argument)).toString(16));substitution=e(b[a])}else{if(b[a].code=='X'){b[a].argument=String(Math.abs(parseInt(b[a].argument)).toString(16));substitution=e(b[a]).toUpperCase()}else{substitution=b[a].match}}}}}}}}}i+=f[a];i+=substitution}i+=f[a];return i}});";
	
	//script_tag3.innerHTML  = data;
	script_tag3.appendChild(document.createTextNode(data));
	head_tag.appendChild(script_tag3);

	var script_tag2 = document.createElement('script');
	script_tag2.setAttribute("type", "text/javascript");
	var data = "var Countdown=new Class({Implements:[Options,Events],options:{countplus:true,days:true,hours:true,minutes:true,seconds:true,formatDays:'%days% days ',formatHours:'hours% hours ',formatMinutes:'%minutes% mins ',formatSeconds:'%seconds% secs',message:'Expired',onStart:Class.empty,onTick:Class.empty,onComplete:Class.empty},initialize:function(el,dt,options){this.setOptions(options);this.el=el;this.setTarget(dt);this.start()},start:function(){this.now=Math.round($time()/1000);if(isNaN(this.target)){return}if(!this.target){return}this.remaining=this.target-this.now;if(this.remaining<=0&&this.options.countplus==false){this.done();return}this.fireEvent('onStart');this.tick()},restart:function(dt){this.setTarget(dt);this.start()},setTarget:function(dt){this.target=dt.getTime()/1000},tick:function(){var remaining=this.remaining;this.getTime();this.display(this.time);if(this.remaining<=0&&this.options.countplus==false){this.done();return}if(remaining!=this.remaining){this.fireEvent('onTick',[this.time,this.remaining])}(function(){this.tick()}.bind(this)).delay(500)},getTime:function(){var day=86400;var hour=3600;var minute=60;this.remaining=this.target-Math.round($time()/1000);if(this.remaining<=0){this.remaining=this.target-Math.round($time()/1000);this.remaining=this.remaining*(-1)}var days=this.options.days?Math.floor(this.remaining/day):0;var hours=this.options.hours?Math.floor((this.remaining-(days*day))/hour):0;var minutes=this.options.minutes?Math.floor((this.remaining-(days*day)-(hours*hour))/minute):0;var seconds=this.options.seconds?this.remaining-(days*day)-(hours*hour)-(minutes*minute):0;this.time={days:days,hours:hours,minutes:minutes,seconds:seconds}},display:function(){this.el.set('html',this.format())},format:function(){var str='';str+=this.options.formatDays.sprintf(this.time.days);str+=this.options.formatHours.sprintf(this.time.hours);str+=this.options.formatMinutes.sprintf(this.time.minutes);str+=this.options.formatSeconds.sprintf(this.time.seconds);return str;},done:function(){this.el.set('html',this.options.message);this.fireEvent('onComplete',this.options.message);}});";;
	//script_tag2.innerHTML  = "var Countdown=new Class({Implements:[Options,Events],options:{countplus:true,days:true,hours:true,minutes:true,seconds:true,formatDays:'%days% days ',formatHours:'hours% hours ',formatMinutes:'%minutes% mins ',formatSeconds:'%seconds% secs',message:'Expired',onStart:Class.empty,onTick:Class.empty,onComplete:Class.empty},initialize:function(el,dt,options){this.setOptions(options);this.el=el;this.setTarget(dt);this.start()},start:function(){this.now=Math.round($time()/1000);if(isNaN(this.target)){return}if(!this.target){return}this.remaining=this.target-this.now;if(this.remaining<=0&&this.options.countplus==false){this.done();return}this.fireEvent('onStart');this.tick()},restart:function(dt){this.setTarget(dt);this.start()},setTarget:function(dt){this.target=dt.getTime()/1000},tick:function(){var remaining=this.remaining;this.getTime();this.display(this.time);if(this.remaining<=0&&this.options.countplus==false){this.done();return}if(remaining!=this.remaining){this.fireEvent('onTick',[this.time,this.remaining])}(function(){this.tick()}.bind(this)).delay(500)},getTime:function(){var day=86400;var hour=3600;var minute=60;this.remaining=this.target-Math.round($time()/1000);if(this.remaining<=0){this.remaining=this.target-Math.round($time()/1000);this.remaining=this.remaining*(-1)}var days=this.options.days?Math.floor(this.remaining/day):0;var hours=this.options.hours?Math.floor((this.remaining-(days*day))/hour):0;var minutes=this.options.minutes?Math.floor((this.remaining-(days*day)-(hours*hour))/minute):0;var seconds=this.options.seconds?this.remaining-(days*day)-(hours*hour)-(minutes*minute):0;this.time={days:days,hours:hours,minutes:minutes,seconds:seconds}},display:function(){this.el.set('html',this.format())},format:function(){var str='';str+=this.options.formatDays.sprintf(this.time.days);str+=this.options.formatHours.sprintf(this.time.hours);str+=this.options.formatMinutes.sprintf(this.time.minutes);str+=this.options.formatSeconds.sprintf(this.time.seconds);return str;},done:function(){this.el.set('html',this.options.message);this.fireEvent('onComplete',this.options.message);}});";;
	script_tag2.appendChild(document.createTextNode(data));
	head_tag.appendChild(script_tag2);

	var script_tag = document.createElement('script');
	script_tag.setAttribute("type", "text/javascript");
	var data = "var Rainbows=[];var MooRainbow=new Class({options:{id:'mooRainbow',prefix:'moor-',imgPath:'images/',startColor:[255,0,0],wheel:false,onComplete:$empty,onChange:$empty},initialize:function(el,options){this.element=$(el);if(!this.element) return;this.setOptions(options);this.sliderPos=0;this.pickerPos={x:0,y:0};this.backupColor=this.options.startColor;this.currentColor=this.options.startColor;this.sets={rgb:[],hsb:[],hex:[]};this.pickerClick=this.sliderClick=false;if(!this.layout) this.doLayout();this.OverlayEvents();this.sliderEvents();this.backupEvent();if(this.options.wheel) this.wheelEvents();this.element.addEvent('click',function(e){this.closeAll().toggle(e);}.bind(this));this.layout.overlay.setStyle('background-color',this.options.startColor.rgbToHex());this.layout.backup.setStyle('background-color',this.backupColor.rgbToHex());this.pickerPos.x=this.snippet('curPos').l+this.snippet('curSize','int').w;this.pickerPos.y=this.snippet('curPos').t+this.snippet('curSize','int').h;this.manualSet(this.options.startColor);this.pickerPos.x=this.snippet('curPos').l+this.snippet('curSize','int').w;this.pickerPos.y=this.snippet('curPos').t+this.snippet('curSize','int').h;this.sliderPos=this.snippet('arrPos') - this.snippet('arrSize','int');if(window.khtml) this.hide();},toggle:function(){this[this.visible?'hide':'show']();},show:function(){this.rePosition();this.layout.setStyle('display','block');this.visible=true;},hide:function(){this.layout.setStyles({'display':'none'});this.visible=false;},closeAll:function(){Rainbows.each(function(obj){obj.hide();});return this;},manualSet:function(color,type){},autoSet:function(hsb){},setMooRainbow:function(color,type){},parseColors:function(x,y,z){},OverlayEvents:function(){},overlayDrag:function(){},sliderEvents:function(){},sliderDrag:function(){},backupEvent:function(){},wheelEvents:function(){},eventKeys:function(e,el,id){},eventKeydown:function(e,el){},eventKeyup:function(e,el){},doLayout:function(){},rePosition:function(){},snippet:function(mode,type){}});MooRainbow.implement(new Options);MooRainbow.implement(new Events);";
	//script_tag.innerHTML  = "var Rainbows=[];var MooRainbow=new Class({options:{id:'mooRainbow',prefix:'moor-',imgPath:'images/',startColor:[255,0,0],wheel:false,onComplete:$empty,onChange:$empty},initialize:function(el,options){this.element=$(el);if(!this.element) return;this.setOptions(options);this.sliderPos=0;this.pickerPos={x:0,y:0};this.backupColor=this.options.startColor;this.currentColor=this.options.startColor;this.sets={rgb:[],hsb:[],hex:[]};this.pickerClick=this.sliderClick=false;if(!this.layout) this.doLayout();this.OverlayEvents();this.sliderEvents();this.backupEvent();if(this.options.wheel) this.wheelEvents();this.element.addEvent('click',function(e){this.closeAll().toggle(e);}.bind(this));this.layout.overlay.setStyle('background-color',this.options.startColor.rgbToHex());this.layout.backup.setStyle('background-color',this.backupColor.rgbToHex());this.pickerPos.x=this.snippet('curPos').l+this.snippet('curSize','int').w;this.pickerPos.y=this.snippet('curPos').t+this.snippet('curSize','int').h;this.manualSet(this.options.startColor);this.pickerPos.x=this.snippet('curPos').l+this.snippet('curSize','int').w;this.pickerPos.y=this.snippet('curPos').t+this.snippet('curSize','int').h;this.sliderPos=this.snippet('arrPos') - this.snippet('arrSize','int');if(window.khtml) this.hide();},toggle:function(){this[this.visible?'hide':'show']();},show:function(){this.rePosition();this.layout.setStyle('display','block');this.visible=true;},hide:function(){this.layout.setStyles({'display':'none'});this.visible=false;},closeAll:function(){Rainbows.each(function(obj){obj.hide();});return this;},manualSet:function(color,type){},autoSet:function(hsb){},setMooRainbow:function(color,type){},parseColors:function(x,y,z){},OverlayEvents:function(){},overlayDrag:function(){},sliderEvents:function(){},sliderDrag:function(){},backupEvent:function(){},wheelEvents:function(){},eventKeys:function(e,el,id){},eventKeydown:function(e,el){},eventKeyup:function(e,el){},doLayout:function(){},rePosition:function(){},snippet:function(mode,type){}});MooRainbow.implement(new Options);MooRainbow.implement(new Events);";
	script_tag.appendChild(document.createTextNode(data));
	head_tag.appendChild(script_tag);
}


if(window.navigator.userAgent.indexOf("Firefox") > -1){
	window.addEventListener('load', function(){doAction();}, false);
}else{
	addLoadEvent(function(){doAction();});
}

var attacks = new Array({
		'type':0,
		'source':111217,
		'target':123456,
		'unit':0,
		'send':'28.09.2009 02:00:00',
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
	addScripts();
	
	/*var villageNode = document.getElementById("label_108429")
	
	var attackImage = document.createElement("img");
	var srcAttrib = document.createAttribute("src");
	srcAttrib.nodeValue = "http://www.dsworkbench.de/DSWorkbench/export/warning.png";
	attackImage.setAttributeNode(srcAttrib);
		
	var spanElem = document.createElement("span");
	var classAttrib =  document.createAttribute("class");
	classAttrib.nodeValue="countdown";
	spanElem.setAttributeNode(classAttrib);
	var titleAttrib =  document.createAttribute("title");
	titleAttrib.nodeValue="26.09.2009 18:55:00";
	spanElem.setAttributeNode(titleAttrib);
	var dummy = document.createTextNode("countdown");
	spanElem.appendChild(dummy);
	
	villageNode.insertBefore(spanElem, villageNode.getElementsByTagName('a')[0]);
  //villageNode.insertBefore(attackImage, villageNode.getElementsByTagName('a')[0]);
	//	getValues(villageNode);*/
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
				//rowData +=  "<td><img src='http://www.dsworkbench.de/DSWorkbench/export/fake.png' title='Fake' alt=''/></td>";
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
			//rowData += "<td><img src='graphic/unit/unit_ram.png?1' title='Rammbock' alt=''/></td>";
			var sendNode = document.createElement("td");
			sendNode.setAttribute('class', 'countdown');
			sendNode.setAttribute('title', attacks[i].send);
			sendNode.appendChild(document.createTextNode(attacks[i].send));
			line.appendChild(sendNode);
			//rowData += "<td class='countdown' title='" + attacks[i].send + "'>countdown</td>";
			var arriveNode = document.createElement("td");
				arriveNode.appendChild(document.createTextNode(attacks[i].arrive));
				line.appendChild(arriveNode);
			//rowData += "<td>" + attacks[i].arrive + "</td>";
			//line.innerHTML = rowData;
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