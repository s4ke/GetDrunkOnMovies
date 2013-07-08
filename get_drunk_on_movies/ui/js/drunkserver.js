$(document).ready(function() {
	$('#test').click(function() {
		hideElement($('#test'));
	});
	
	apeInit();
	apeWalk();
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
		$.getJSON("http://localhost:8080/?allMovies", {}, 
			function(data) {
				var container = $('#ds-content');
				var sorted = {};
				sortMovies(data, sorted);
				hideElement(container, function() {
					container.empty();
					putMoviesInList(sorted, container);
					showElement(container);
				});
			}
		);
}

function putMoviesInList(movies, list) {
	var divider = $('<div class="ds-ui-divider"></div>');
	var movie = $('<span class="ds-ui-entry"></div>');
	for(var index in movies) {
		var odd = true;
		// divider
		var tmp = divider.clone();
		tmp.html(index);
		list.append(tmp);
		// movies
		for(var key in movies[index]) {
			var movieClone = movie.clone();
			movieClone.html(movies[index][key]);
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
	$.getJSON("http://localhost:8080/", {
			'movie' : movie
		}, function(data) {
			var display = $('#display');
			display.html('');
			for(var i = 0; i < data.length; ++i) {
				var instance = data[i];
				addMovieToDiv(display, instance);
			}
			addEvents();
		});
}
		
function addMovieToDiv(div, movie) {
	var drink = movie['drink'];
	var name = movie['name'];
	if(drink !== null && name !== null) {
		var movieTitleContainer = $('<div class="movieTitle">' + name + '</div>');
		movieTitleContainer.data('name', name);
		div.append(movieTitleContainer);
		var drinkContainer = $('<div>');
		for(var occasion in drink) {
			var drinkDiv = $('<div>');
			drinkDiv.attr('class', 'drink');
			drinkDiv.append(occasion + ' : ' + drink[occasion]);
			drinkContainer.append(drinkDiv);
		}
		div.append(drinkContainer);
	}
}
		
function addEvents() {
	$('.movieTitle').click(function(event) {
		showOne($(event.target).data('name'));
	});
}