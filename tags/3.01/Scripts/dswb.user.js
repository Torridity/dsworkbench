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

var api = typeof unsafeWindow != 'undefined' ? unsafeWindow.ScriptAPI : window.ScriptAPI;
api.register( 'DS Workbench Userscript', 7.4, 'Torridity', 'nospam@dsworkbench.de' );

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


/**Select villages that have been inserted to the custom input field */
function selectVillages(){
	try{
		var ids = document.getElementById('village_ids').value.split(';');
		var valueSet = 0;
		for (var i = 0; i < ids.length; i++){
			var id = ids[i];
			if(id != null && id.length > 3){
				valueSet++;
	  		$x('//input[@value='+ id + ']')[0].checked = true;
	 		}
		}
		if(valueSet > 0){
			//at least one village was found, set input fields background to green
			document.getElementById('village_ids').setAttribute('style', 'background-color:#00FF00');
		}else{
			//no village was found, mark input field red
			document.getElementById('village_ids').setAttribute('style', 'background-color:#FF0000');
		}
	}catch(err){
		//some error has occured, mark field red
		document.getElementById('village_ids').setAttribute('style', 'background-color:#FF0000');
	}
}

//parse arguments
function getArgs() { 
	var table = document.getElementById('group_assign_table');
	if(table != null){
		//we are in the groups view, add groups input field
		var input = document.createElement('input');
		input.setAttribute('class', 'vis');
		input.setAttribute('id', 'village_ids');
		input.setAttribute('size', '45');
		input.setAttribute('value', '<Bitte Dorf-IDs einfuegen>');
		input.addEventListener('keyup', function(){
			selectVillages();
		}, false);
		table.appendChild(input);
  	return;
 	}

  //default handling of browser URL
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

  if (args.type){
  	//get type field to decide which entries are expected (troops or resources)
		type = parseInt(args.type);
	}else{
		//no valid type found
	 	type = -1;
	}

	if(type == 0){
		//add troops
  	doInsertAction(new Array("spear", "sword", "axe", "archer", "spy", "light", "marcher", "heavy", "ram", "catapult", "knight", "snob"));
	}else if(type == 1){
		//add resources
  	doInsertAction(new Array("wood", "stone", "iron"));
	}
} 

/**Insert a list of elements by their names*/
function doInsertAction(expectedElements){
	//go through all expected elements
   for (var i = 0; i < expectedElements.length; ++i){
	    //get form field for expected element	    
	    field = document.getElementsByName(expectedElements[i])[0];
   
	    //if field was found and arguments contains a value for the expected element...
	    if(field != null && args[expectedElements[i]] != null){
	      //...insert value
				field.value=args[expectedElements[i]];
      }
   }
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

