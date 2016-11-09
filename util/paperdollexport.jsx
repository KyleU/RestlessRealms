// enable double clicking from the Macintosh Finder or the Windows Explorer
#target photoshop

// in case we double clicked the file
app.bringToFront();

var msg = "";

var doc = app.activeDocument;

$.level = 2;

var saveOptions = new PNGSaveOptions();
saveOptions.interlaced = 0;

var gender = null;
if(doc.name.indexOf("female") > -1) {
	gender = "female";
} else if(doc.name.indexOf("male") > -1) {
	gender = "male";
} else {
	throw "Unknown gender! Must end in -male or -female";
}

var folder = new Folder(doc.path + "/../war/img/paperdoll/" + gender);
if(!folder.exists) {
    throw ("Folder \"" + folder.absoluteURI + "\" does not exist.");
}

function getLayerSetsIndex(){
   function getNumberLayers(){
	   var ref = new ActionReference();
	   ref.putProperty( charIDToTypeID("Prpr") , charIDToTypeID("NmbL") )
	   ref.putEnumerated( charIDToTypeID("Dcmn"), charIDToTypeID("Ordn"), charIDToTypeID("Trgt") );
	   return executeActionGet(ref).getInteger(charIDToTypeID("NmbL"));
   }
   function hasBackground() {
       var ref = new ActionReference();
       ref.putProperty( charIDToTypeID("Prpr"), charIDToTypeID( "Bckg" ));
       ref.putEnumerated(charIDToTypeID( "Lyr " ),charIDToTypeID( "Ordn" ),charIDToTypeID( "Back" ))//bottom Layer/background
       var desc =  executeActionGet(ref);
       var res = desc.getBoolean(charIDToTypeID( "Bckg" ));
       return res   
    };
   function getLayerType(idx,prop) {        
       var ref = new ActionReference();
       //ref.putProperty( 1349677170 , prop);
       ref.putIndex(charIDToTypeID( "Lyr " ), idx);
       var desc =  executeActionGet(ref);
       var type = desc.getEnumerationValue(prop);
       var res = typeIDToStringID(type);
       return res   
    };
   var cnt = getNumberLayers()+1;
   var res = new Array();
   if(hasBackground()){
        var i = 0;
	}else{
		var i = 1;
	};
   var prop =  stringIDToTypeID("layerSection") 
   for(i;i<cnt;i++){
      var temp = getLayerType(i,prop);
      if(temp == "layerSectionStart") res.push(i);
   };
   return res;
};

function makeActiveByIndex( idx, visible ){
    var desc = new ActionDescriptor();
      var ref = new ActionReference();
      ref.putIndex(charIDToTypeID( "Lyr " ), idx)
      desc.putReference( charIDToTypeID( "null" ), ref );
      desc.putBoolean( charIDToTypeID( "MkVs" ), visible );
   executeAction( charIDToTypeID( "slct" ), desc, DialogModes.NO);
};

var groups = getLayerSetsIndex();

var start = new Date();

for(var i = groups.length; i > 0; i--) {
    makeActiveByIndex(groups[i - 1], false);
    var currentLayer = doc.activeLayer;
	currentLayer.visible = false;
}

var stop = new Date();

var scanTime = (stop - start) / 1000 ;

msg += (groups.length + " folder sets... I can probably do the work in " + (scanTime * 8) + " seconds or so...\r");

var category = null;
var categoryFolder = null;
var subcategory = null;
var lastItem = null;
var numItems = 0;
var numCategoryItems = 0;

for(var i = groups.length; i > 0; i--) {
    makeActiveByIndex(groups[i - 1], false);
    var currentLayer = doc.activeLayer;

	if(currentLayer.parent.typename == "Document") {
		category = currentLayer;
		msg += category.name + "\r";
		categoryFolder = new Folder(folder.absoluteURI + "/" + category.name);
		if(!categoryFolder.exists) {
			categoryFolder.create();
		}
		category.visible = true;
	} else if(currentLayer.parent == category) {
		if(subcategory != null) {
			msg += "  - " + category.name + "/" + subcategory.name + " (" + numCategoryItems + " items)\r";
			subcategory.visible = false;
		}
		numCategoryItems = 0;
		subcategory = currentLayer;
		subcategory.visible = true;
	} else if(currentLayer.parent == subcategory) {
		lastItem = currentLayer;
		if(currentLayer.name.indexOf(".") > -1) {
			currentLayer.name = currentLayer.name.replace(".", "-");
		}

		numItems++;
		numCategoryItems++;
		currentLayer.visible = true;		
		var f = new File(categoryFolder.absoluteURI + "/" + currentLayer.name + ".png" )
		doc.saveAs(f, saveOptions, true, Extension.LOWERCASE);
		currentLayer.visible = false;
	} else if(currentLayer.parent == lastItem) {
		//no op
	} 
}
msg += "  - " + category.name + "/" + subcategory.name + " (" + numCategoryItems + " items)\r";
msg += "\r" + numItems + " items processed in total. You're welcome.";
alert(msg);

/*

var numItems = 0;
for(var i = 0; i < doc.layerSets.length; i++) {
    var category = doc.layerSets[i];
    category.visible = false;
    for(var j = 0; j < category.layerSets.length; j++) {
        var categoryType = category.layerSets[j];
        categoryType.visible = false;
        for(var k = 0; k < categoryType.layerSets.length; k++) {
            var categoryItem = categoryType.layerSets[k];
			numItems++;
			if(numItems % 50 == 0) {
				alert(numItems + "...");
			}
            categoryItem.visible = false;
            if(categoryItem.name.indexOf(".") > -1) {
                categoryItem.name = categoryItem.name.replace(".", "-");
            }
		}
    }
}
alert("Prepare to write " + numItems + " optimized PNGs...");

for(var i = 0; i < doc.layerSets.length; i++) {
    var category = doc.layerSets[i];    
    msg += category.name + "\r";
    var categoryFolder = new Folder(folder.absoluteURI + "/" + category.name);
    if(!categoryFolder.exists) {
        categoryFolder.create();
    }
    for(var j = 0; j < category.layerSets.length; j++) {
        var categoryType = category.layerSets[j];
        var numRows = 0;
		
        for(var k = 0; k < categoryType.layerSets.length; k++) {
            var categoryItem = categoryType.layerSets[k];
			categoryItem.visible = true;
			var f = new File(categoryFolder.absoluteURI + "/" + categoryItem.name + ".png" )
			doc.saveAs(f, saveOptions, true, Extension.LOWERCASE);
            categoryItem.visible = false;
			numRows++
		}
        msg += "  - " + category.name + "/" + categoryType.name + " (" + numRows + " items)\r";
        categoryType.visible = false;
    }
}
alert(msg);

*/