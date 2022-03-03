/*
 * list = [{name:xxxx,value:yyyy}.....]
 * options = {container:xxxxxxx, maxSelect:sssssss, defaultSelected:index}
 * container a class
 * */

function GRVMSelect(list,options){
	var object = this;
	var container = $("."+options.container);
	var selectedItems = 0;
	var selectedObjects = [];
	var maxSelect = options.maxSelect;
	
	var scontainer = $("<div>",{class:"grvmselect-container"}).appendTo(container);
	var sbar = $("<div>",{class:"grvmselect-bar"}).appendTo(scontainer);
	var sclick = $("<div>",{class:"grvmselect-click"}).append($("<i>",{class:"fa fa-caret-down"})).appendTo(scontainer);
	var slist = $("<div>",{class:"grvmselect-list"}).appendTo($("#wraper"));
	
	if(typeof(options.defaultSelected) != "undefined"){
		addToBar(makeItemBar(list[options.defaultSelected]));
	}
	
	populateList();
	
	sclick.click(function(){
		var p = $(this).parent().offset(); 
		slist.css("top",p.top+$(this).parent().height()+3);
		slist.css("left",p.left);
		slist.css("width",$(this).parent().width());
		$(slist).toggle();
	}).on('click', 'div', function(e) {
	    // clicked on descendant div
	    e.stopPropagation();
	});
	
	function populateList(){
		$.each(list,function(i,v){
			$("<div>",{class:"grvmselect-list-item"}).append(makeItemList(v)).appendTo(slist);
		});
	}
	
	
	function addToBar(obj){
		var v = obj.find(".grvmselect-item-label").attr("grv-item-value");
		var selected = sbar.find(".grvmselect-item-container");
		if(selected.length < maxSelect){
			var isSelected = false;
			$.each(selected,function(j,k){
				var l = $(k).find(".grvmselect-item-label");
				if(l.attr("grv-item-value") == v)isSelected = true;
			});
			if(!isSelected){
				sbar.find("span").remove();
				sbar.append(obj);
				selectedItems++;
				selectedObjects.push(list[v]);
				$(object).change();
			}
		}else{
			alert("Maximum items selected is 2");
		}
	}
	
	function makeItemBar(itemObj){
		var itemContainer = $("<div>",{class:"grvmselect-item-container"});
		var itemLabel = $("<div>",{class:"grvmselect-item-label","grv-item-value":itemObj.value}).text(itemObj.name).appendTo(itemContainer);
		var itemClear = $("<div>",{class:"grvmselect-item-clear"}).append($("<i>",{class:"fa fa-times"})).appendTo(itemContainer);
		itemClear.click(function(e){
			e.stopPropagation();
			if($(this).parent().find(".grvmselect-item-label").attr("grv-item-value") == "0" && options.idrole!=1){
				alert("Not allowed to remove All communities item");
			}else{
				var value = $(this).parent().find(".grvmselect-item-label").attr("grv-item-value");
				var indexDelItem = -1 ; 
				$.each(selectedObjects,function(i,v){
					if(v.value == value){
						indexDelItem = i;
					}
				});
				selectedObjects.splice(indexDelItem,1);
				selectedItems --;
				$(object).change();
				$(this).parent().remove();
				var selected = sbar.find(".grvmselect-item-container");
				if(selected.length == 0){
					sbar.append($("<span>",{class:"grvmselect-span"}).text("Select one or two communities"));
				}
			}
		});
		return itemContainer;
	}
	
	function makeItemList(itemObj){
		var itemContainer = $("<div>",{class:"grvmselect-item-container"});
		$("<div>",{class:"grvmselect-item-label","grv-item-value":itemObj.value}).text(itemObj.name).appendTo(itemContainer);
		var p1 = {"param1":itemObj};
		itemContainer.click(p1, function(e){
			addToBar(makeItemBar(e.data.param1));
			slist.toggle();
		});
		return itemContainer;
	}
	
	return {
		selectedObjects : selectedObjects,
		object : object,
		getValue : function(){
			var result = "";
			$.each(selectedObjects,function(i,v){
				if(i==0){
					result = v.value;
				}else{
					result += "_"+v.value
				}
			});
			return result;
		}
	}
}