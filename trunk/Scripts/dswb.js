// ==UserScript==
// @name DS - Stammespunkteverlauf
// @namespace none
// @include http://de*.die-staemme.de/*screen=place*
// ==/UserScript==

var args = getArgs(); 
if (args.type){
		type = parseInt(args.type);
}else{
	 type = -1;
}
	
if(type == 0){
   //all available units
   units = new Array("spear", "sword", "axe", "archer", "spy", "light", "marcher", "heavy", "ram", "catapult", "knight", "snob");
   //go through all units

   alert(document.getElementsByName("support")[0]);
   for (var i = 0; i < units.length; ++i){
	    //get field for unit
	    field = document.getElementsByName(units[i])[0];
	    //if field is valid and arguments contain value for field
	    if(field != null && args[units[i]] != null){
	       //insert valid value
         insertUnit(field, parseInt(args[units[i]]));
      }
   }
}

//parse arguments
function getArgs() { 
   var args = new Object(); 
   var query = location.search.substring(1); 
   var pairs = query.split("&"); 
   for(var i = 0; i < pairs.length; i++) { 
      var pos = pairs[i].indexOf('='); 
      if (pos == -1) continue; 
         var argname = pairs[i].substring(0,pos); 
         var value = pairs[i].substring(pos+1); 
         args[argname] = unescape(value); 
      } 
   return args; 
} 




/**helper function**/

/*function insertUnit(input, count) {
	if(input.value != count)
		input.value=count;
	else
		input.value='';
}*/

function attack(){
	document.forms.units.attack.click();
}

function getGameDoc() {
    getdoc = window.document;
    
    if(!getdoc.URL.match('game\.php')) {
        for(var i=0; i<window.frames.length; i++) {
            if(window.frames[i].document.URL.match('game\.php')) {
                getdoc = window.frames[i].document;
            }
        }
    }
    
    return getdoc;
}