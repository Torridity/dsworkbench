// ==UserScript==
// @name           DS Workbench Scripts
// @namespace      none
// @include        http://de*.die-staemme.de/game.php?*screen=place*
// @include        http://de*.die-staemme.de/game.php?screen=place*
// @include        http://de*.die-staemme.de/game.php?*screen=place
// @include        http://de*.die-staemme.de/game.php?*screen=market&mode=send*
// @include        http://de*.die-staemme.de/game.php?*mode=groups*
// @exclude        http://de*.die-staemme.de/game.php?*screen=place&mode=units
// @exclude        http://de*.die-staemme.de/game.php?*screen=place&mode=sim
// @exclude        http://de*.die-staemme.de/game.php?*screen=place&mode=neighbor
// @exclude        http://de*.die-staemme.de/game.php?*screen=place&try=confirm
// ==/UserScript==

var $x = function(p, context) {
	if(!context){
		context = document;
	}
	var i, arr = [], xpr = document.evaluate(p, context, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
	for (i = 0; item = xpr.snapshotItem(i); i++)
		arr.push(item);
	return arr;
};
	
if(window.navigator.userAgent.indexOf("Firefox") > -1){
	window.addEventListener('load', function() { 
		getArgs();}, false);
	}else{
		addLoadEvent(function() {
  	getArgs();
	});
}


function selectVillages(){
	try{
		var ids = document.getElementById('village_ids').value.split(';');

		for (var i = 0; i < ids.length; i++){
			var id = ids[i];
			if(id != null && id.length>3){
	  		$x('//input[@value='+ id + ']')[0].checked = true;
	 		}
		}
	}catch(err){
		alert("Fehler waehrend der Dorfauswahl.");
	}
}

//parse arguments
function getArgs() { 
	if(document.getElementById('group_assign_table') != null){
		//groups handling
		var menu = document.getElementById('overview_menu');
		var input = document.createElement('input');
		input.setAttribute('id', 'village_ids');
		menu.appendChild(input);
  	var button = document.createElement('button');
		button.textContent = 'Doerfer waehlen';
		button.addEventListener('click', function(){
			selectVillages();
		}, false);
	
  	menu.appendChild(button);
  	return;
 	}
  
   args = new Object();
   var query = location.search.substring(1); 

   var pairs = query.split("&"); 
   for(var i = 0; i < pairs.length; i++) { 
      var pos = pairs[i].indexOf('='); 
      if (pos == -1) continue; 
         var argname = pairs[i].substring(0,pos); 
         var value = pairs[i].substring(pos+1); 
         args[argname] = unescape(value); 
      } 
   //return args; 
  if (args.type){
		type = parseInt(args.type);
	}else{
	 type = -1;
	}

	if(type == 0){
  	doInsertUnitsAction();
	}else if(type == 1){
  	doInsertResourcesAction();
	}
} 

function doInsertUnitsAction(){      
 //all available units
   units = new Array("spear", "sword", "axe", "archer", "spy","light", "marcher", "heavy", "ram", "catapult", "knight", "snob");
   //go through all units

   for (var i = 0; i < units.length; ++i){
	    //get field for unit	    
	    field = document.getElementsByName(units[i])[0];
   
	    //if field is valid and arguments contains value for field
	    if(field != null && args[units[i]] != null){
	       //insert value
				field.value=args[units[i]];
      }
   }
}

function doInsertResourcesAction(){
  //insert resource value
	document.getElementsByName('wood')[0].value=args['wood'];
	document.getElementsByName('stone')[0].value=args['clay'];
	document.getElementsByName('iron')[0].value=args['iron'];
}


function addLoadEvent(func) {
	var oldonload;
	if(window.navigator.userAgent.indexOf("Firefox") > -1){
		oldonload = unsafeWindow.onload;
	}else{
		oldonload = window.onload
	}


  if (typeof window.onload != 'function') {
  	window.onload = func;
  } else {
    window.onload = function() {
      if (oldonload) {
        oldonload();
      }
      func();
    }
  }
}

