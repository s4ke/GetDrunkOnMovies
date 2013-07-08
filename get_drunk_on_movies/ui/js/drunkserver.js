host = 'http://78.47.27.135';

var delay = (function(){
  var timer = 0;
  return function(callback, ms){
    clearTimeout (timer);
    timer = setTimeout(callback, ms);
  };
})();

$(document).ready(function() {
	$('#test').click(function() {
		hideElement($('#test'));
	});
	
	$('#ds-searchbox').keyup(function() {
		delay(function(){
			var searchString = $('#ds-searchbox').val();
			if(searchString !== undefined && searchString !== "") {
				search(searchString);
			}
		}, 500 );
	});
	
	
	
	//function() {
		//var searchString = $('#ds-searchbox').val();
		//if(searchString !== undefined && searchString !== "") {
		//	search(searchString);
		//}
	//});
	
	//apeInit();
	//apeWalk();
});

function apeInit() {
	var ape = $('#ape');
	ape.css({
		'top' : '30',
		'left' : '-164'
	});
}

function apeWalk() {
	var ape = $('#ape');
	ape.removeClass('ds-ui-ape-flip');
	ape.delay(800).animate({'left' : $(window).width()}, 20000, apeWalkBack);
}

function apeWalkBack() {
	var ape = $('#ape');
	ape.addClass('ds-ui-ape-flip');
	ape.delay(800).animate({'left' : -64}, 20000, apeWalk);
}

function hideElement(ele, callback) {
	$(ele).css('position', 'relative');
	$(ele).animate({
			'opacity' : '0.0',
			'top' : '30'
		}, 
		400, 
		function() {
			$(ele).css({
				'position'	: '', 
				'top' 		: '0',
				'display' 	: 'none'
			});
			if(callback !== undefined) {
				callback();
			}
		}
	);
}

function showElement(ele, callback) {
	$(ele).css({
		'position'	: 'relative', 
		'top' 		: '-30',
		'display' 	: '',
		'opacity'	: '0'
	});
	$(ele).animate({
			'opacity' : '1.0',
			'top' : '0'
		}, 
		400, 
		function() {
			$(ele).css('position' , '');
			if(callback !== undefined) {
				callback();
			}
		}
	);
}

function showMovieList() {
	$.getJSON(host+":8080/?allMovies", 
		{}, 
		fillMovieList
	);
}

function fillMovieList(data) {
	var container = $('#ds-content');
	var sorted = {};
	sortMovies(data, sorted);
	hideElement(container, function() {
		container.empty();
		putMoviesInList(sorted, container);
		showElement(container);
	});
}

function putMoviesInList(movies, list) {
	var divider = $('<div class="ds-ui-divider"></div>');
	var movie = $('<div class="ds-ui-entry ds-ui-clickable"></div>');
	for(var index in movies) {
		var odd = true;
		// divider
		var tmp = divider.clone();
		tmp.html(index);
		list.append(tmp);
		// movies
		for(var key in movies[index]) {
			var movieClone = movie.clone();
			movieClone.data('name', movies[index][key]);
			movieClone.html(movies[index][key]);
			movieClone.click(function(e) {
				showOne($(e.target).data('name'));
			});
			if(odd) {
				movieClone.addClass('ds-ui-entry-odd');
			}
			odd = !odd;
			tmp.after(movieClone);
		}	
	}
}

function sortMovies(movies, sorted) {
	for(var key in movies) {
		var index = movies[key].charAt(0).toLowerCase();
		var cur = sorted[index];
		if(cur === undefined) {
			sorted[index] = new Array();
			sorted[index][0] = movies[key];
		} else {
			cur.push(movies[key]);
		}
	} 
}
		
function showOne(movie) {
	$.getJSON(host+":8080/", 
	{
		'movie' : movie
	}, function(data) {
		var container = $('#ds-content');
		hideElement(container, function() {
			container.empty();
			for(var key in data) {
				var instance = data[key];
				addMovieToDiv(container, instance);
			}
			showElement(container);
		});			
	});
}
		
function addMovieToDiv(container, movie) {	
	var titleContainer = $('<div class="ds-ui-divider"></div>');
	var table = $('<table class="ds-ui-drink-table"></table>');
	var tableRow = $('<tr class="ds-ui-entry"></tr>');
	var occasionContainer = $('<td class="ds-ui-occasion"></td>');
	var drinkCountContainer = $('<td class="ds-ui-drink-count"></td>');
	
	var drink = movie['drink'];
	var name = movie['name'];
	if(drink !== null && name !== null) {
		var titleClone = titleContainer.clone();
		titleClone.html(name);
		container.append(titleClone);
		var odd = true;
		for(var occasion in drink) {
			var rowClone = tableRow.clone();
			var occasionClone = occasionContainer.clone();
			var drinkCountClone = drinkCountContainer.clone();
			
			if(odd) {
				rowClone.addClass('ds-ui-entry-odd');
			}
			
			occasionClone.html(occasion);
			drinkCountClone.html(drink[occasion]);
			rowClone.append(occasionClone);
			rowClone.append(drinkCountClone);
			table.append(rowClone);
			
			odd = !odd;
		}
		
		container.append(table);
	}
}

function search(searchString) {
	$.getJSON(host+":8080/", 
	{
		'search' : searchString
	}, 
	fillMovieList);
}
		
function addEvents() {
	$('.movieTitle').click(function(event) {
		showOne($(event.target).data('name'));
	});
}